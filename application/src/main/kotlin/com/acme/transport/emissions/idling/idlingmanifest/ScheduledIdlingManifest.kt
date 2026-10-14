package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled idling manifest records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a scheduled idling manifest is in its life cycle. */
enum class ScheduledIdlingManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a scheduled idling manifest and its current status. */
data class ScheduledIdlingManifest(
    var code: String,
    var title: String,
    var margin: Double,
    var loadKg: Double,
    var description: String,
    var label: String,
    var pallets: Int,
    var maximum: Double,
    var status: ScheduledIdlingManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledIdlingManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledIdlingManifestStatus): Boolean {
        when (status) {
            ScheduledIdlingManifestStatus.PROPOSED -> {
                when (next) {
                    ScheduledIdlingManifestStatus.APPROVED, ScheduledIdlingManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScheduledIdlingManifestStatus.APPROVED -> return next == ScheduledIdlingManifestStatus.WITHDRAWN
            ScheduledIdlingManifestStatus.REJECTED -> {
                when (next) {
                    ScheduledIdlingManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledIdlingManifestStatus) {
        if (!canMoveTo(next)) {
            error("scheduled idling manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun projectedVolume(): Double = round((maximum - margin) * 100) / 100

    fun snapshot(): ScheduledIdlingManifest {
        return ScheduledIdlingManifest(
            code = code,
            title = title,
            margin = margin,
            loadKg = loadKg,
            description = description,
            label = label,
            pallets = pallets,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled idling manifest in status proposed. */
fun newScheduledIdlingManifest(code: String, title: String): ScheduledIdlingManifest {
    return ScheduledIdlingManifest(
        code = code,
        title = title,
        margin = 0.0,
        loadKg = 0.0,
        description = "",
        label = "",
        pallets = 0,
        maximum = 0.0,
        status = ScheduledIdlingManifestStatus.PROPOSED,
    )
}
