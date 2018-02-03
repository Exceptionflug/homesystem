package de.exceptionflug.homesystem.utils

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard

import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


object ReflectionUtil {

    val version: String
        get() = Bukkit.getServer().javaClass.`package`.name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[3]

    @Throws(ClassNotFoundException::class)
    fun getClass(classname: String): Class<*> {
        val path = classname.replace("{nms}", "net.minecraft.server." + version).replace("{obc}", "org.bukkit.craftbukkit." + version)
                .replace("{nm}", "net.minecraft." + version)
        return Class.forName(path)
    }

    @Throws(NoSuchMethodException::class, SecurityException::class, IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
    fun getNMSPlayer(p: Player): Any {
        val getHandle = p.javaClass.getMethod("getHandle")
        return getHandle.invoke(p)
    }

    @Throws(SecurityException::class, IllegalArgumentException::class, ClassNotFoundException::class)
    fun getOBCPlayer(p: Player): Any {
        return getClass("{obc}.entity.CraftPlayer").cast(p)
    }

    @Throws(NoSuchMethodException::class, SecurityException::class, IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
    fun getNMSWorld(w: World): Any {
        val getHandle = w.javaClass.getMethod("getHandle")
        return getHandle.invoke(w)
    }

    @Throws(NoSuchMethodException::class, SecurityException::class, IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
    fun getNMSScoreboard(s: Scoreboard): Any {
        val getHandle = s.javaClass.getMethod("getHandle")
        return getHandle.invoke(s)
    }

    @Throws(IllegalArgumentException::class, IllegalAccessException::class, NoSuchFieldException::class, SecurityException::class)
    fun getFieldValue(instance: Any, fieldName: String): Any {
        val field = instance.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(instance)
    }

    fun <T> getFieldValue(field: Field, obj: Any): T? {
        try {
            return field.get(obj) as T
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    @Throws(Exception::class)
    fun getField(clazz: Class<*>, fieldName: String): Field {
        val field = clazz.getDeclaredField(fieldName)
        field.isAccessible = true
        return field
    }

    fun setValue(instance: Any, field: String, value: Any) {
        try {
            val f = instance.javaClass.getDeclaredField(field)
            f.isAccessible = true
            f.set(instance, value)
        } catch (t: Throwable) {
            t.printStackTrace()
        }

    }

    operator fun setValue(c: Class<*>, instance: Any, field: String, value: Any) {
        try {
            val f = c.getDeclaredField(field)
            f.isAccessible = true
            f.set(instance, value)
        } catch (t: Throwable) {
            t.printStackTrace()
        }

    }

    fun setValueSubclass(clazz: Class<*>, instance: Any, field: String, value: Any) {
        try {
            val f = clazz.getDeclaredField(field)
            f.isAccessible = true
            f.set(instance, value)
        } catch (t: Throwable) {
            t.printStackTrace()
        }

    }

    @Throws(Exception::class)
    fun sendAllPacket(packet: Any) {
        for (p in Bukkit.getOnlinePlayers()) {
            val nmsPlayer = getNMSPlayer(p)
            val connection = nmsPlayer.javaClass.getField("playerConnection").get(nmsPlayer)
            connection.javaClass.getMethod("sendPacket", ReflectionUtil.getClass("{nms}.Packet")).invoke(connection, packet)
        }
    }

    fun sendListPacket(players: List<String>, packet: Any) {
        try {
            for (name in players) {
                val nmsPlayer = getNMSPlayer(Bukkit.getPlayer(name))
                val connection = nmsPlayer.javaClass.getField("playerConnection").get(nmsPlayer)
                connection.javaClass.getMethod("sendPacket", ReflectionUtil.getClass("{nms}.Packet")).invoke(connection, packet)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }

    }

    @Throws(Exception::class)
    fun sendPlayerPacket(p: Player, packet: Any) {
        val nmsPlayer = getNMSPlayer(p)
        val connection = nmsPlayer.javaClass.getField("playerConnection").get(nmsPlayer)
        connection.javaClass.getMethod("sendPacket", ReflectionUtil.getClass("{nms}.Packet")).invoke(connection, packet)
    }

    fun listFields(e: Any) {
        println(e.javaClass.name + " contains " + e.javaClass.declaredFields.size + " declared fields.")
        println(e.javaClass.name + " contains " + e.javaClass.declaredClasses.size + " declared classes.")
        val fds = e.javaClass.declaredFields
        for (i in fds.indices) {
            fds[i].isAccessible = true
            try {
                println(fds[i].name + " -> " + fds[i].get(e))
            } catch (e1: IllegalArgumentException) {
                e1.printStackTrace()
            } catch (e1: IllegalAccessException) {
                e1.printStackTrace()
            }

        }
    }

    @Throws(IllegalAccessException::class, NoSuchFieldException::class)
    fun getFieldValue(superclass: Class<*>, instance: Any, fieldName: String): Any {
        val field = superclass.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(instance)
    }
}
