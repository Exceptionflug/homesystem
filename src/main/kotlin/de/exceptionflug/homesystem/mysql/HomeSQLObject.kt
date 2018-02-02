package de.exceptionflug.homesystem.mysql

import de.exceptionflug.homesystem.home.Home
import java.util.*
import kotlin.collections.HashSet

class HomeSQLObject(private val connectionHolder: ConnectionHolder) : SQLObject("homesystem_home", connectionHolder) {

    var id: Int? = null
        private set
    var uuid: String = ""
    var owner: String? = null
    private var members: String = ""
    var world: String = ""
    var xCord: Double = 0.0
    var yCord: Double = 0.0
    var zCord: Double = 0.0
    var yaw: Float = 0F
    var pitch: Float = 0F

    fun getMembers(): Set<UUID> {
        val members = HashSet<UUID>()
        val memberSplit = this.members.split(";")
        memberSplit.mapTo(members) {UUID.fromString(it)}
        return members
    }

    fun setMembers(members: Set<UUID>) {
        val builder = StringBuilder()
        var index = 0
        for(member in members) {
            index ++;
            builder.append(member)
            if(index != members.size) {
                builder.append(';')
            }
        }
        this.members = builder.toString()
    }

    companion object {
        fun getByUUID(uuid: UUID, connectionHolder: ConnectionHolder): HomeSQLObject? {
            val home = HomeSQLObject(connectionHolder)
            home.uuid = uuid.toString()
            home["uuid"]
            return if(home.id == null) null else home
        }
        fun getByDBID(id: Int, connectionHolder: ConnectionHolder): HomeSQLObject? {
            val home = HomeSQLObject(connectionHolder)
            home.id = id
            home["id"]
            return if(home.owner == null) null else home
        }
    }

}