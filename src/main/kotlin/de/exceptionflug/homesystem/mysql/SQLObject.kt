package de.exceptionflug.homesystem.mysql

import org.bukkit.Bukkit
import java.sql.SQLException

open class SQLObject protected constructor(private val tableName: String, private val connectionHolder: ConnectionHolder) {

    @Throws(SQLException::class)
    fun save() {
        val sb = StringBuilder("UPDATE $tableName SET ")
        val fields = this.javaClass.declaredFields
        for (f in fields) {
            if(f.name == "Companion" || f.type == ConnectionHolder::class.java) continue
            val name = if (f.name.startsWith("_")) f.name.substring(1) else f.name
            sb.append(name + "=?,")
            f.isAccessible = true
        }
        try {
            val idf = this.javaClass.getDeclaredField("id")
            idf.isAccessible = true
            val sql = sb.toString().substring(0, sb.length - 1) + (" WHERE id=" + idf.get(this))
            val ps = connectionHolder.prepareStatement(sql)
            var realIndex = 0
            for (i in 1 until fields.size + 1) {
                if(fields[i-1].name == "Companion" || fields[i-1].type == ConnectionHolder::class.java) continue
                realIndex ++
                try {
                    ps.setObject(realIndex, fields[i - 1].get(this))
                } catch (e: IllegalAccessException) {
                    Bukkit.getLogger().severe("Failed to access field " + fields[i].name + " of " + this.javaClass.name)
                    continue
                } catch (e: Exception) {
                    continue
                }

            }
            connectionHolder.executeUpdate(ps)
            ps.close()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            Bukkit.getLogger().severe("Failed to save " + this.javaClass.name + ": There is no id field in the class.")
        }

    }

    @Throws(SQLException::class)
    operator fun get(vararg providedInfo: String) {
        val whereClause = StringBuilder()
        for (i in providedInfo.indices) {
            whereClause.append(if (providedInfo[i].startsWith("_")) providedInfo[i].substring(1) else providedInfo[i])
            whereClause.append("=?")
            if (i != providedInfo.size - 1) {
                whereClause.append(" AND ")
            }
        }
        val ps = connectionHolder.prepareStatement("SELECT * FROM " + tableName + " WHERE " + whereClause.toString())
        try {
            for (i in providedInfo.indices) {
                val f = this.javaClass.getDeclaredField(providedInfo[i])
                f.isAccessible = true
                ps.setObject(i + 1, f.get(this))
            }
            val rs = connectionHolder.executeQuery(ps)
            while (rs.next()) {
                val fields = this.javaClass.declaredFields
                for (ff in fields) {
                    if(ff.name == "Companion" || ff.type == ConnectionHolder::class.java) continue
                    val name = if (ff.name.startsWith("_")) ff.name.substring(1) else ff.name
                    ff.isAccessible = true
                    ff.set(this, rs.getObject(name))
                }
            }
            rs.close()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } finally {
            ps.close()
        }
    }

    @Throws(SQLException::class, IllegalAccessException::class)
    fun insert() {
        val sb = StringBuilder("INSERT INTO $tableName (")
        val fields = this.javaClass.declaredFields
        var i = 0
        for (f in fields) {
            i++
            if(f.name == "Companion" || f.type == ConnectionHolder::class.java) {
                if (i == fields.size) {
                    sb.deleteCharAt(sb.length - 1)
                    sb.append(") VALUES (")
                }
                continue
            }
            f.isAccessible = true
            if (f.get(this) == null) {
                if (i == fields.size) {
                    sb.deleteCharAt(sb.length - 1)
                    sb.append(") VALUES (")
                }
                continue
            }
            val name = if (f.name.startsWith("_")) f.name.substring(1) else f.name
            if (i == fields.size) {
                sb.append(name + ") VALUES (")
            } else {
                sb.append(name + ",")
            }
        }
        i = 0
        for (f in fields) {
            i++
            if(f.name == "Companion" || f.type == ConnectionHolder::class.java) {
                if (i == fields.size) {
                    sb.deleteCharAt(sb.length - 1)
                    sb.append(")")
                }
                continue
            }
            if (f.get(this) == null) {
                if (i == fields.size) {
                    sb.deleteCharAt(sb.length - 1)
                    sb.append(")")
                }
                continue
            }
            if (i == fields.size) {
                sb.append("?)")
            } else {
                sb.append("?,")
            }
        }
        val ps = connectionHolder.prepareStatement(sb.toString())
        i = 0
        for (f in fields) {
            if(!f.isAccessible) f.isAccessible = true
            if (f.get(this) == null || f.name == "Companion" || f.type == ConnectionHolder::class.java) {
                continue
            }
            i++
            ps.setObject(i, f.get(this))
        }
        connectionHolder.executeUpdate(ps)
        ps.close()
    }

    @Throws(SQLException::class)
    fun createTable() {
        val builder = StringBuilder("id int NOT NULL AUTO_INCREMENT, PRIMARY KEY (id), ")
        val fields = this.javaClass.declaredFields
        var i = 0
        for (ff in fields) {
            i++
            val name = if (ff.name.startsWith("_")) ff.name.substring(1) else ff.name
            if(ff.type == ConnectionHolder::class.java || ff.name == "Companion") {
                if (i == fields.size) {
                    builder.deleteCharAt(builder.length - 2)
                    builder.append(")")
                }
                continue
            }
            if (!name.equals("id", ignoreCase = true)) {
                builder.append(name).append(" ").append(getType(ff.type))
            }
            if (i != fields.size) {
                if (!name.equals("id", ignoreCase = true)) {
                    builder.append(", ")
                }
            } else {
                builder.append(")")
            }
        }
        val sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + builder.toString()
        val ps = connectionHolder.prepareStatement(sql)
        connectionHolder.executeUpdate(ps)
        ps.close()
    }

    private fun getType(clazz: Class<*>): String {
        return if (clazz == String::class.java) {
            "text"
        } else if (clazz == Int::class.java) {
            "int"
        } else if (clazz == Long::class.java) {
            "bigint"
        } else if (clazz == Double::class.java) {
            "double"
        } else if (clazz == Float::class.java) {
            "float"
        } else if (clazz == Float::class.javaPrimitiveType) {
            "float"
        } else if (clazz == Int::class.javaPrimitiveType) {
            "int"
        } else if (clazz == Double::class.javaPrimitiveType) {
            "double"
        } else if (clazz == Long::class.javaPrimitiveType) {
            "bigint"
        } else if (clazz == Boolean::class.javaPrimitiveType) {
            "tinyint"
        } else if (clazz == Boolean::class.java) {
            "tinyint"
        } else {
            throw IllegalArgumentException("Can not serialize " + clazz)
        }
    }

    fun delete() {
        val ps = connectionHolder.prepareStatement("DELETE FROM $tableName WHERE id=?")
        val fid = this.javaClass.getDeclaredField("id")
        if(!fid.isAccessible) {
            fid.isAccessible = true
        }
        ps.setObject(1, fid.get(this))
        connectionHolder.executeUpdate(ps)
        ps.close()
    }

}
