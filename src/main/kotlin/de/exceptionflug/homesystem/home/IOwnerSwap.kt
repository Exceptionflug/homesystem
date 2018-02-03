package de.exceptionflug.homesystem.home

interface IOwnerSwap {

    fun getHome1(): Home?
    fun getHome2(): Home?
    fun getTimestamp(): Long

}