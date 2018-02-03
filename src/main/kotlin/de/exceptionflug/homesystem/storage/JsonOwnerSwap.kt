package de.exceptionflug.homesystem.storage

import de.exceptionflug.homesystem.HomeSystemPlugin
import de.exceptionflug.homesystem.home.Home
import de.exceptionflug.homesystem.home.IOwnerSwap
import java.util.*

class JsonOwnerSwap(private var uuid1: UUID, private var uuid2: UUID, private var time: Long) : IOwnerSwap {

    override fun getHome1(): Home? {
        return HomeSystemPlugin.getInstance().homeStorage.load(uuid1)
    }

    override fun getHome2(): Home? {
        return HomeSystemPlugin.getInstance().homeStorage.load(uuid2)
    }

    override fun getTimestamp(): Long {
        return time
    }
}