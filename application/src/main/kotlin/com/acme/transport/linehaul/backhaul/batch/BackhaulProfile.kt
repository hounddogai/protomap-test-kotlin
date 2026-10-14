package com.acme.transport.linehaul.backhaul.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul profile is in its life cycle. */
enum class BackhaulProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a backhaul profile and its current status. */
data class BackhaulProfile(
    var code: String,
    var label: String,
    var createdAt: Int,
    var contactEmail: String,
    var budget: Double,
    var limit: Int,
    var ceiling: Double,
    var category: String,
    var dock: String,
    var attributes: MutableMap<String, String>,
    var status: BackhaulProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulProfileStatus): Boolean {
        when (status) {
            BackhaulProfileStatus.PLANNED -> return next == BackhaulProfileStatus.COMPLETED
            BackhaulProfileStatus.SCHEDULED -> {
                when (next) {
                    BackhaulProfileStatus.IN_PROGRESS, BackhaulProfileStatus.CANCELLED -> return true
                }
                return false
            }
            BackhaulProfileStatus.IN_PROGRESS -> {
                when (next) {
                    BackhaulProfileStatus.COMPLETED, BackhaulProfileStatus.CANCELLED -> return true
                }
                return false
            }
            BackhaulProfileStatus.COMPLETED -> {
                when (next) {
                    BackhaulProfileStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulProfileStatus) {
        if (!canMoveTo(next)) {
            error("backhaul profile $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedExposure(): Double = round((budget - limit.toDouble()) * 100) / 100

    fun snapshot(): BackhaulProfile {
        return BackhaulProfile(
            code = code,
            label = label,
            createdAt = createdAt,
            contactEmail = contactEmail,
            budget = budget,
            limit = limit,
            ceiling = ceiling,
            category = category,
            dock = dock,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a backhaul profile in status planned. */
fun newBackhaulProfile(code: String, label: String): BackhaulProfile {
    return BackhaulProfile(
        code = code,
        label = label,
        createdAt = 0,
        contactEmail = "",
        budget = 0.0,
        limit = 0,
        ceiling = 0.0,
        category = "",
        dock = "",
        attributes = mutableMapOf<String, String>(),
        status = BackhaulProfileStatus.PLANNED,
    )
}
