package de.exceptionflug.homesystem.storage

import de.exceptionflug.homesystem.home.Home
import java.util.*

interface IHomeStorage {

    fun save(home: Home)
    fun load(id: UUID): Home?

}