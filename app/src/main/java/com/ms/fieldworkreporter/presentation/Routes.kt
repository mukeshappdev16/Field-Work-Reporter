package com.ms.fieldworkreporter.presentation

import kotlinx.serialization.Serializable

sealed interface Routes {
    @Serializable
    data object Home : Routes

    @Serializable
    data class TaskDetail(
        val id: Long? = null,
        val title: String,
        val description: String
    ) : Routes
}
