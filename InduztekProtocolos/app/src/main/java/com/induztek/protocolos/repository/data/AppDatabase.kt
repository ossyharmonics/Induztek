package com.induztek.protocolos.repository.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.induztek.protocolos.repository.models.Empresa
import com.induztek.protocolos.repository.models.Prueba
import com.induztek.protocolos.repository.models.TipoUsuario
import com.induztek.protocolos.repository.models.User

/**
 * BASE DE DATOS ROOM (capa Model de MVVM).
 *
 * Room crea la base SQLite a partir de las entidades registradas aquí:
 *   - TipoUsuario -> tabla tipo_usuarios (Administrador y Técnico)
 *   - User        -> tabla users
 *   - Prueba      -> tabla pruebas (con FK usuarioId)
 *   - Empresa     -> tabla empresas (catálogo de clientes)
 *
 * ¿Cómo se comunica con el resto del código?
 *   MainActivity -> los ViewModels usan AppDatabase.getDatabase(application)
 *   AppDatabase  -> expone los DAO (pruebaDao, userDao, tipoUsuarioDao, empresaDao)
 *   DAO          -> ejecuta las consultas SQL y devuelve Flow o datos suspend
 *
 * version = 3 porque:
 *   - La v1 solo tenía pruebas.
 *   - La v2 agregó users, tipo_usuarios y la FK de autoría.
 *   - La v3 agrega empresas y elimina el rol "Empresa" del seed.
 * fallbackToDestructiveMigration: al cambiar de versión se recrea la base.
 * En esta versión se conserva únicamente el usuario administrador inicial.
 */
@Database(
    entities = [Prueba::class, User::class, TipoUsuario::class, Empresa::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Cada método entrega el DAO correspondiente al ViewModel que lo necesite.
    abstract fun pruebaDao(): PruebaDao
    abstract fun userDao(): UserDao
    abstract fun tipoUsuarioDao(): TipoUsuarioDao
    abstract fun empresaDao(): EmpresaDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val NOMBRE_DB = "induztek_protocolos.db"

        /**
         * Singleton: abre la base UNA sola vez y reutiliza la misma conexión.
         * Todas las pantallas y ViewModels deben usar esta instancia para no
         * corromper los datos ni abrir conexiones duplicadas.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Nunca una Activity: evita fugas de memoria
                    AppDatabase::class.java,
                    NOMBRE_DB
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(seedCallback)
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * DATOS INICIALES (seed), igual que en el proyecto del profesor.
         *
         * Se ejecuta en onOpen con INSERT OR IGNORE, por lo que:
         *   - Se cargan la primera vez.
         *   - Se vuelven a cargar si una migración destructiva recreó la base.
         *   - No se duplican en aperturas posteriores.
         *
         * Tipo 1 = Administrador, tipo 2 = Técnico.
         * "Empresa" ya no es un rol: ahora es una entidad independiente en
         * la tabla empresas.
         * Usuario inicial: admin@duoc.cl / admin123 con el tipo Administrador.
         * No se siembran más usuarios: solo se conserva el admin original.
         *
         * Orden importante: primero tipo_usuarios y después users, porque la
         * tabla users tiene una FK que apunta a tipo_usuarios.
         */
        private val seedCallback = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)

                db.execSQL(
                    "INSERT OR IGNORE INTO tipo_usuarios (id, tipo_usuario) VALUES " +
                        "(1, 'Administrador'), (2, 'Técnico')"
                )

                db.execSQL(
                    "INSERT OR IGNORE INTO users (name, email, password, profileImageUri, tipoUsuarioId) " +
                        "VALUES ('Administrador', 'admin@duoc.cl', 'admin123', NULL, 1)"
                )
            }
        }
    }
}
