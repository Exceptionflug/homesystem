package de.exceptionflug.homesystem.storage

import de.exceptionflug.homesystem.home.Home
import de.exceptionflug.homesystem.home.IOwnerSwap
import java.util.*
import kotlin.collections.HashSet

interface IHomeStorage {

    fun save(home: Home)
    fun load(id: UUID): Home?
    fun delete(home: Home)
    fun getAll(): Set<Home>
    fun ownerSwap(h1: Home, h2: Home)
    fun getAllSwaps(): Set<IOwnerSwap>

    fun getByOwner(uuid: UUID): Set<Home> {
        val all = getAll()
        val byOwner = HashSet<Home>()
        all.filterTo(byOwner) {it.ownerID == uuid}
        return byOwner
    }

    fun getByMember(uuid: UUID): Set<Home> {
        val all = getAll()
        val byMember = HashSet<Home>()
        all.filterTo(byMember) {it.members.contains(uuid)}
        return byMember
    }

    fun getByMemberWithName(uuid: UUID, name: String): Home? {
        val all = getAll()
        for(home in all) {
            if(home.members.contains(uuid) && home.name == name) return home
        }
        return null
    }

    fun getByName(name: String, uniqueId: UUID): Home? {
        val all = getAll()
        return all.firstOrNull {it.name == name && it.ownerID == uniqueId}
    }
}