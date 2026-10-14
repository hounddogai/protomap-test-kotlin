package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling incident manifest records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a rolling incident manifest is in its life cycle. */
enum class RollingIncidentManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One rolling incident manifest, with what the planners need to act on it. */
data class RollingIncidentManifest(
    var code: String,
    var title: String,
    var minimum: Double,
    var stopCount: Int,
    var attributes: MutableMap<String, String>,
    var notes: String,
    var pallets: Int,
    var phone: String,
    var odometer: Int,
    var visible: Boolean,
    var status: RollingIncidentManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingIncidentManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingIncidentManifestStatus): Boolean {
        when (status) {
            RollingIncidentManifestStatus.DRAFT -> return next == RollingIncidentManifestStatus.PENDING
            RollingIncidentManifestStatus.PENDING -> return next == RollingIncidentManifestStatus.SUSPENDED || next == RollingIncidentManifestStatus.CLOSED
            RollingIncidentManifestStatus.ACTIVE -> {
                when (next) {
                    RollingIncidentManifestStatus.SUSPENDED, RollingIncidentManifestStatus.CLOSED -> return true
                }
                return false
            }
            RollingIncidentManifestStatus.SUSPENDED -> return next == RollingIncidentManifestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingIncidentManifestStatus) {
        if (!canMoveTo(next)) {
            error("rolling incident manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((minimum + stopCount.toDouble()) * 100) / 100

    fun snapshot(): RollingIncidentManifest {
        return RollingIncidentManifest(
            code = code,
            title = title,
            minimum = minimum,
            stopCount = stopCount,
            attributes = attributes,
            notes = notes,
            pallets = pallets,
            phone = phone,
            odometer = odometer,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling incident manifest in status draft. */
fun newRollingIncidentManifest(code: String, title: String): RollingIncidentManifest {
    return RollingIncidentManifest(
        code = code,
        title = title,
        minimum = 0.0,
        stopCount = 0,
        attributes = mutableMapOf<String, String>(),
        notes = "",
        pallets = 0,
        phone = "",
        odometer = 0,
        visible = false,
        status = RollingIncidentManifestStatus.DRAFT,
    )
}
