package de.exceptionflug.homesystem.storage

import de.exceptionflug.homesystem.HomeSystemPlugin
import de.exceptionflug.homesystem.home.Home
import de.exceptionflug.homesystem.mysql.ConnectionHolder
import de.exceptionflug.homesystem.mysql.HomeSQLObject
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.collections.HashSet

class MySQLHomeStorage(private val connectionHolder: ConnectionHolder) : IHomeStorage {

    override fun save(home: Home) {
        Bukkit.getScheduler().runTaskAsynchronously(HomeSystemPlugin.getInstance(), {
            val flag = load(home.id) == null
            val sql = if(flag) HomeSQLObject(connectionHolder) else HomeSQLObject.getByUUID(home.id, connectionHolder)!!
            sql.setMembers(home.members)
            sql.owner = home.ownerID.toString()
            sql.world = home.location.world.name
            sql.xCord = home.location.x
            sql.yCord = home.location.y
            sql.zCord = home.location.z
            sql.yaw = home.location.yaw
            sql.pitch = home.location.pitch
            sql.uuid = home.id.toString()
            if(flag) {
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
                Bukkit.getScheduler().runTaskAsynchronously(HomeSystemPlugin.getInstance(), {
                    val ps = connectionHolder.prepareStatement("SELECT id FROM homesystem_home")
                    val rs = connectionHolder.executeQuery(ps)
                    val homes = HashSet<Home>()
                    while(rs.next()) {
                        val homeSQL = HomeSQLObject.getByDBID(rs.getInt(0), connectionHolder)
                        if(homeSQL != null) {
                            val home = Home(UUID.fromString(homeSQL.uuid), UUID.fromString(homeSQL.owner), Location(Bukkit.getWorld(homeSQL.world), homeSQL.xCord, homeSQL.yCord, homeSQL.zCord, homeSQL.yaw, homeSQL.pitch))
                            for(member in homeSQL.getMembers()) {
                                home.members.add(member)
                            }
                            homes.add(home)
                        }
                    }
                    ps.close()
                    this.homes = homes
                })
                while(!isDone) {
                    if(System.currentTimeMillis() > maxMillis) throw TimeoutException()
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
                Bukkit.getScheduler().runTaskAsynchronously(HomeSystemPlugin.getInstance(), {
                    val homeSQL = HomeSQLObject.getByUUID(uuid, connectionHolder)
                    if(homeSQL != null) {
                        home = Home(UUID.fromString(homeSQL.uuid), UUID.fromString(homeSQL.owner), Location(Bukkit.getWorld(homeSQL.world), homeSQL.xCord, homeSQL.yCord, homeSQL.zCord, homeSQL.yaw, homeSQL.pitch))
                        for(member in homeSQL.getMembers()) {
                            home!!.members.add(member)
                        }
                    }
                    done = true
                })
                while(!isDone) {
                    if(System.currentTimeMillis() > maxMillis) throw TimeoutException()
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