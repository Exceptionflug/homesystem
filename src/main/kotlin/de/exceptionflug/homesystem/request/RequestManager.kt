package de.exceptionflug.homesystem.request

import de.exceptionflug.homesystem.HomeSystemPlugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class RequestManager {

    val requests = HashSet<IRequest>()

    init {
        Bukkit.getScheduler().runTaskTimer(HomeSystemPlugin.getInstance(), {
            val markedForDelete = HashSet<IRequest>()
            for(request in requests) {
                if(System.currentTimeMillis() > request.getTimeoutTime()) {
                    request.timeout()
                    markedForDelete.add(request)
                }
            }
            for(request in markedForDelete) {
                requests.remove(request)
            }
        }, 20, 20)
    }

    fun accept(acceptor: Player, request: IRequest) {
        request.accepted(acceptor)
        requests.remove(request)
    }

    fun reject(rejector: Player, request: IRequest) {
        request.rejected(rejector)
        requests.remove(request)
    }

}