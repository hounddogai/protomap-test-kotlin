package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.text.normalize

// The legacy incident manifest records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a legacy incident manifest is in its life cycle. */
enum class LegacyIncidentManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy incident manifest as the incident team keeps it. */
data class LegacyIncidentManifest(
    var code: String,
    var title: String,
    var driverId: String,
    var capacity: Int,
    var active: Boolean,
    var status: LegacyIncidentManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyIncidentManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyIncidentManifestStatus): Boolean {
        when (status) {
            LegacyIncidentManifestStatus.PLANNED -> return next == LegacyIncidentManifestStatus.CANCELLED
            LegacyIncidentManifestStatus.SCHEDULED -> return next == LegacyIncidentManifestStatus.IN_PROGRESS
            LegacyIncidentManifestStatus.IN_PROGRESS -> return next == LegacyIncidentManifestStatus.CANCELLED
            LegacyIncidentManifestStatus.COMPLETED -> {
                when (next) {
                    LegacyIncidentManifestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyIncidentManifestStatus) {
        if (!canMoveTo(next)) {
            error("legacy incident manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): LegacyIncidentManifest {
        return LegacyIncidentManifest(
            code = code,
            title = title,
            driverId = driverId,
            capacity = capacity,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy incident manifest in status planned. */
fun newLegacyIncidentManifest(code: String, title: String): LegacyIncidentManifest {
    return LegacyIncidentManifest(
        code = code,
        title = title,
        driverId = "",
        capacity = 0,
        active = false,
        status = LegacyIncidentManifestStatus.PLANNED,
    )
}
