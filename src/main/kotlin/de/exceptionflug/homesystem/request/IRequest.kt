package de.exceptionflug.homesystem.request

import org.bukkit.entity.Player

interface IRequest {

    fun rejected(player: Player)
    fun accepted(player: Player)
    fun timeout()
    fun getTimeoutTime(): Long

}