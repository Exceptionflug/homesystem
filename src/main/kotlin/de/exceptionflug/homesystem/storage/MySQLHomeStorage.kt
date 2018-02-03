package de.exceptionflug.homesystem.storage

import de.exceptionflug.homesystem.HomeSystemPlugin
import de.exceptionflug.homesystem.home.Home
import de.exceptionflug.homesystem.home.IOwnerSwap
import de.exceptionflug.homesystem.mysql.ConnectionHolder
import de.exceptionflug.homesystem.mysql.HomeSQLObject
import de.exceptionflug.homesystem.mysql.OwnerSwapSQLObject
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.*
import java.util.concurrent.*
import kotlin.collections.HashSet

class MySQLHomeStorage(private val connectionHolder: ConnectionHolder) : IHomeStorage {

    private val pool: Executor = Executors.newCachedThreadPool()

    override fun save(home: Home) {
        Bukkit.getScheduler().runTaskAsynchronously(HomeSystemPlugin.getInstance(), {
            val flag = load(home.id) == null
            val sql = if (flag) HomeSQLObject(connectionHolder) else HomeSQLObject.getByUUID(home.id, connectionHolder)!!
            sql.setMembers(home.members)
            sql.name = home.name
            sql.owner = home.ownerID.toString()
            sql.world = home.location.world.name
            sql.xCord = home.location.x
            sql.yCord = home.location.y
            sql.zCord = home.location.z
            sql.yaw = home.location.yaw
            sql.pitch = home.location.pitch
            sql.uuid = home.id.toString()
            if (flag) {
                sql.insert()
            } else {
                sql.save()
            }
        })
    }

    override fun load(id: UUID): Home? {
        return loadAsync(id).get()
    }

    override fun getAll(): Set<Home> {
        return getAllAsync().get()
    }

    override fun ownerSwap(h1: Home, h2: Home) {
        val obj1 = HomeSQLObject.getByUUID(h1.id, connectionHolder) ?: throw NullPointerException("Home with id ${h1.id} is not stored in the backend")
        val obj2 = HomeSQLObject.getByUUID(h2.id, connectionHolder) ?: throw NullPointerException("Home with id ${h2.id} is not stored in the backend")
        val uuid1 = obj1.owner
        val uuid2 = obj2.owner
        obj1.owner = uuid2
        obj2.owner = uuid1
        obj1.save()
        obj2.save()
        val swap = OwnerSwapSQLObject(connectionHolder)
        swap.time = System.currentTimeMillis()
        swap.uuid1 = h1.id.toString()
        swap.uuid2 = h2.id.toString()
        swap.insert()
        h1.ownerID = UUID.fromString(uuid2)
        h2.ownerID = UUID.fromString(uuid1)
    }

    override fun getAllSwaps(): Set<IOwnerSwap> {
        return getAllSwapsAsync().get()
    }

    override fun delete(home: Home) {
        val obj = HomeSQLObject.getByUUID(home.id, connectionHolder) ?: throw NullPointerException("Home with id ${home.id} is not stored in the backend")
        obj.delete()
    }

    fun getAllSwapsAsync(): Future<Set<OwnerSwapSQLObject>> {
        return object : Future<Set<OwnerSwapSQLObject>> {

            private var swaps: Set<OwnerSwapSQLObject>? = null

            override fun isDone(): Boolean {
                return swaps != null
            }

            override fun get(): Set<OwnerSwapSQLObject> {
                return get(30, TimeUnit.SECONDS)
            }

            override fun get(timeout: Long, unit: TimeUnit?): Set<OwnerSwapSQLObject> {
                val maxMillis = System.currentTimeMillis() + unit!!.toMillis(timeout)
                pool.execute({
                    val ps = connectionHolder.prepareStatement("SELECT id FROM homesystem_swap")
                    val rs = connectionHolder.executeQuery(ps)
                    val swaps = HashSet<OwnerSwapSQLObject>()
                    while (rs.next()) {
                        val sql = OwnerSwapSQLObject.getByDBID(rs.getInt(1), connectionHolder)
                        if (sql != null) {
                            swaps.add(sql)
                        }
                    }
                    ps.close()
                    this.swaps = swaps
                })
                while (!isDone) {
                    if (System.currentTimeMillis() > maxMillis) throw TimeoutException()
                    Thread.yield()
                }
                return swaps!!
            }

            override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
                return false
            }

            override fun isCancelled(): Boolean {
                return false
            }

        }
    }

    fun getAllAsync(): Future<Set<Home>> {
        return object : Future<Set<Home>> {

            private var homes: Set<Home>? = null

            override fun isDone(): Boolean {
                return homes != null
            }

            override fun get(): Set<Home> {
                return get(30, TimeUnit.SECONDS)
            }

            override fun get(timeout: Long, unit: TimeUnit?): Set<Home> {
                val maxMillis = System.currentTimeMillis() + unit!!.toMillis(timeout)
                pool.execute({
                    val ps = connectionHolder.prepareStatement("SELECT id FROM homesystem_home")
                    val rs = connectionHolder.executeQuery(ps)
                    val homes = HashSet<Home>()
                    while (rs.next()) {
                        val homeSQL = HomeSQLObject.getByDBID(rs.getInt(1), connectionHolder)
                        if (homeSQL != null) {
                            val home = Home(UUID.fromString(homeSQL.uuid), UUID.fromString(homeSQL.owner), Location(Bukkit.getWorld(homeSQL.world), homeSQL.xCord, homeSQL.yCord, homeSQL.zCord, homeSQL.yaw, homeSQL.pitch))
                            for (member in homeSQL.getMembers()) {
                                home.members.add(member)
                            }
                            home.name = homeSQL.name!!
                            homes.add(home)
                        }
                    }
                    ps.close()
                    this.homes = homes
                })
                while (!isDone) {
                    if (System.currentTimeMillis() > maxMillis) throw TimeoutException()
                    Thread.yield()
                }
                return homes!!
            }

            override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
                return false
            }

            override fun isCancelled(): Boolean {
                return false
            }

        }
    }

    fun loadAsync(uuid: UUID): Future<Home?> {
        return object : Future<Home?> {

            private var home: Home? = null
            private var done: Boolean = false

            override fun isDone(): Boolean {
                return done
            }

            override fun get(): Home? {
                return get(30, TimeUnit.SECONDS)
            }

            override fun get(timeout: Long, unit: TimeUnit?): Home? {
                val maxMillis = System.currentTimeMillis() + unit!!.toMillis(timeout)
                pool.execute({
                    val homeSQL = HomeSQLObject.getByUUID(uuid, connectionHolder)
                    if (homeSQL != null) {
                        home = Home(UUID.fromString(homeSQL.uuid), UUID.fromString(homeSQL.owner), Location(Bukkit.getWorld(homeSQL.world), homeSQL.xCord, homeSQL.yCord, homeSQL.zCord, homeSQL.yaw, homeSQL.pitch))
                        for (member in homeSQL.getMembers()) {
                            home!!.members.add(member)
                        }
                        home!!.name = homeSQL.name!!
                    }
                    done = true
                })
                while (!isDone) {
                    if (System.currentTimeMillis() > maxMillis) throw TimeoutException()
                    Thread.yield()
                }
                return home
            }

            override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
                return false
            }

            override fun isCancelled(): Boolean {
                return false
            }

        }
    }

}