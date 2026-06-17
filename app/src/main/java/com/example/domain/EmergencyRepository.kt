package com.example.domain

import java.util.UUID

data class EmergencyContact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val relation: String,
    val phone: String,
    val isSynced: Boolean = false
)

interface EmergencyRepository {
    suspend fun getContacts(userId: String): kotlinx.coroutines.flow.Flow<List<EmergencyContact>>
    suspend fun addContact(userId: String, contact: EmergencyContact)
    suspend fun updateContact(userId: String, contact: EmergencyContact)
    suspend fun deleteContact(userId: String, contactId: String)
}
