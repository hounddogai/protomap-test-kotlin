package com.acme.transport.emissions.idling.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending idling manifest records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a pending idling manifest is in its life cycle. */
enum class PendingIdlingManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a pending idling manifest and its current status. */
data class PendingIdlingManifest(
    var code: String,
    var label: String,
    var limit: Int,
    var revision: Int,
    var createdAt: Int,
    var category: String,
    var fuelLiters: Double,
    var status: PendingIdlingManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingIdlingManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingIdlingManifestStatus): Boolean {
        when (status) {
            PendingIdlingManifestStatus.OPEN -> {
                when (next) {
                    PendingIdlingManifestStatus.REOPENED, PendingIdlingManifestStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingIdlingManifestStatus.IN_REVIEW -> return next == PendingIdlingManifestStatus.REOPENED
            PendingIdlingManifestStatus.RESOLVED -> return next == PendingIdlingManifestStatus.REOPENED || next == PendingIdlingManifestStatus.ARCHIVED
            PendingIdlingManifestStatus.REOPENED -> return next == PendingIdlingManifestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PendingIdlingManifestStatus) {
        if (!canMoveTo(next)) {
            error("pending idling manifest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun netExposure(): Double = round((limit.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): PendingIdlingManifest {
        return PendingIdlingManifest(
            code = code,
            label = label,
            limit = limit,
            revision = revision,
            createdAt = createdAt,
            category = category,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending idling manifest in status open. */
fun newPendingIdlingManifest(code: String, label: String): PendingIdlingManifest {
    return PendingIdlingManifest(
        code = code,
        label = label,
        limit = 0,
        revision = 0,
        createdAt = 0,
        category = "",
        fuelLiters = 0.0,
        status = PendingIdlingManifestStatus.OPEN,
    )
}
