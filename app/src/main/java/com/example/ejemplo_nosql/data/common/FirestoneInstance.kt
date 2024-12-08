package com.example.ejemplo_nosql.data.common

import com.google.firebase.firestore.FirebaseFirestore

object FirestoneInstance {

    fun get(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}