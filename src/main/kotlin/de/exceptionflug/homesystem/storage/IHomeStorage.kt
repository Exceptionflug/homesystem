package de.exceptionflug.homesystem.storage

import de.exceptionflug.homesystem.home.Home
import java.util.*
import kotlin.collections.HashSet

interface IHomeStorage {

    fun save(home: Home)
    fun load(id: UUID): Home?
    fun getAll(): Set<Home>

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

}