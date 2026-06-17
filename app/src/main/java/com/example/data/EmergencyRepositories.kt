package com.example.data

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.EmergencyContactEntity
import com.example.domain.EmergencyContact
import com.example.domain.EmergencyRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class DefaultEmergencyRepository(private val context: Context) : EmergencyRepository {
    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "medisphere-database"
    ).fallbackToDestructiveMigration(dropAllTables = true).build()

    private val dao = db.emergencyContactDao()

    private val isFirebaseInitialized = try {
        FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null
    } catch (e: Exception) {
        false
    }
    private val firestore by lazy { if (isFirebaseInitialized) FirebaseFirestore.getInstance() else null }

    override suspend fun getContacts(userId: String): Flow<List<EmergencyContact>> {
        syncWithCloud(userId)
        return dao.getAllContacts().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addContact(userId: String, contact: EmergencyContact) {
        dao.insertContact(contact.toEntity())
        syncWithCloud(userId)
    }

    override suspend fun updateContact(userId: String, contact: EmergencyContact) {
        dao.insertContact(contact.toEntity())
        syncWithCloud(userId)
    }

    override suspend fun deleteContact(userId: String, contactId: String) {
        dao.deleteContactById(contactId)
        firestore?.collection("users")?.document(userId)?.collection("contacts")?.document(contactId)?.delete()
    }

    private suspend fun syncWithCloud(userId: String) {
        if (firestore == null) return
        try {
            val unsynced = dao.getUnsyncedContacts()
            if (unsynced.isNotEmpty()) {
                val batch = firestore!!.batch()
                unsynced.forEach { entity ->
                    val docRef = firestore!!.collection("users").document(userId).collection("contacts").document(entity.id)
                    batch.set(docRef, entity.toMap())
                }
                batch.commit().await()
                dao.markAsSynced(unsynced.map { it.id })
            }

            val snapshot = firestore!!.collection("users").document(userId).collection("contacts").get().await()
            val cloudContacts = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val relation = doc.getString("relation") ?: return@mapNotNull null
                val phone = doc.getString("phone") ?: return@mapNotNull null

                EmergencyContactEntity(
                    id = doc.id,
                    name = name,
                    relation = relation,
                    phone = phone,
                    isSynced = true
                )
            }
            if (cloudContacts.isNotEmpty()) {
                dao.clearAllContacts()
                dao.insertContacts(cloudContacts)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun EmergencyContactEntity.toDomain() = EmergencyContact(
        id = id, name = name, relation = relation, phone = phone, isSynced = isSynced
    )

    private fun EmergencyContact.toEntity() = EmergencyContactEntity(
        id = id, name = name, relation = relation, phone = phone, isSynced = isSynced
    )

    private fun EmergencyContactEntity.toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "relation" to relation,
        "phone" to phone
    )
}
