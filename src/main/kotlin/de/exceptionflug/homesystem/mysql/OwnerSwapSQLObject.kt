package de.exceptionflug.homesystem.mysql

import de.exceptionflug.homesystem.HomeSystemPlugin
import de.exceptionflug.homesystem.home.Home
import de.exceptionflug.homesystem.home.IOwnerSwap
import java.util.*

class OwnerSwapSQLObject(connectionHolder: ConnectionHolder) : SQLObject("homesystem_swap", connectionHolder), IOwnerSwap {

    var id: Int? = null
    var uuid1: String? = null
    var uuid2: String? = null
    var time: Long = 0

    override fun getHome1(): Home? {
        return HomeSystemPlugin.getInstance().homeStorage.load(UUID.fromString(uuid1))
    }

    override fun getHome2(): Home? {
        return HomeSystemPlugin.getInstance().homeStorage.load(UUID.fromString(uuid2))
    }

    override fun getTimestamp(): Long {
        return time
    }

    companion object {
        fun getByDBID(id: Int, connectionHolder: ConnectionHolder): OwnerSwapSQLObject? {
            val out = OwnerSwapSQLObject(connectionHolder)
            out.id = id
            out["id"]
            return if(out.uuid1 == null) null else out
        }
    }

}