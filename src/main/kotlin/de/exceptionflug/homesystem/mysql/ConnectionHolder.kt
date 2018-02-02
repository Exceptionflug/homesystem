package de.exceptionflug.homesystem.mysql

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.net.URI

import java.sql.*
import java.util.logging.Level


class ConnectionHolder(val plugin: JavaPlugin) {

    private var connection: Connection? = null
    val lock = Any()
    private var taskRunning: Boolean = false

    fun connect(host: String?, database: String?, port: Int?, user: String?, password: String?) {
        synchronized(lock) {
            try {
                Class.forName("com.mysql.jdbc.Driver")
            } catch (e: ClassNotFoundException) {
                Bukkit.getLogger().log(Level.SEVERE, "MySQL drivers are not working", e)
                return
            }

            try {
                connection = DriverManager.getConnection("jdbc:mysql://$host:$port/$database?user=$user&password=$password")
                if (!taskRunning) {
                    Bukkit.getScheduler().runTaskTimer(plugin, {
                        synchronized (lock) {
                            taskRunning = true
                            close()
                            connection = null
                            System.gc() // Since the JDBC driver is a pure memory leak, we have to rebuild our connection every minute to save memory
                            connect(host, database, port, user, password)
                        }
                    }, (20 * 60).toLong(), (20 * 60).toLong())
                }
            } catch (e: SQLException) {
                Bukkit.getLogger().log(Level.SEVERE, "Connection to MySQL server failed", e)
            }
        }
    }

    private fun close() {
        synchronized(lock) {
            try {
                connection!!.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Threadsafe implementation of connection.prepareStatement
     *
     * @param sql - SQL command
     * @return
     * @throws SQLException if an error occurs
     */
    @Throws(SQLException::class)
    fun prepareStatement(sql: String): PreparedStatement {
        synchronized(lock) {
            return connection!!.prepareStatement(sql)
        }
    }

    /**
     * Threadsafe implementation of preparedstatement.executeQuery
     *
     * @param ps - PreparedStatement
     * @return
     * @throws SQLException if an error occurs
     */
    @Throws(SQLException::class)
    fun executeQuery(ps: PreparedStatement): ResultSet {
        synchronized(lock) {
            return ps.executeQuery()
        }
    }

    /**
     * Threadsafe implementation of preparedstatement.executeUpdate
     *
     * @param ps - PreparedStatement
     * @return
     * @throws SQLException if an error occurs
     */
    @Throws(SQLException::class)
    fun executeUpdate(ps: PreparedStatement): Int {
        synchronized(lock) {
            return ps.executeUpdate()
        }
    }

    fun connect(host: URI) {
        synchronized(lock) {
            try {
                Class.forName("com.mysql.jdbc.Driver")
            } catch (e: ClassNotFoundException) {
                Bukkit.getLogger().log(Level.SEVERE, "MySQL drivers are not working", e)
                return
            }

            try {
                connection = DriverManager.getConnection(host.toString())
                if (!taskRunning) {
                    Bukkit.getScheduler().runTaskTimer(plugin, {
                        synchronized (lock) {
                            taskRunning = true
                            close()
                            connection = null
                            System.gc() // Since the JDBC driver is a pure memory leak, we have to rebuild our connection every minute to save memory
                            connect(host)
                        }
                    }, (20 * 60).toLong(), (20 * 60).toLong())
                }
            } catch (e: SQLException) {
                Bukkit.getLogger().log(Level.SEVERE, "Connection to MySQL server failed", e)
            }
        }
    }

}
