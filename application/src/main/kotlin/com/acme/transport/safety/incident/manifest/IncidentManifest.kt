package com.acme.transport.safety.incident.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The incident manifest records of safety's incident work, and the rules, stores, and calculations around
// them.

/** Where a incident manifest is in its life cycle. */
enum class IncidentManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a incident manifest and its current status. */
data class IncidentManifest(
    var code: String,
    var title: String,
    var limit: Int,
    var depot: String,
    var capacity: Int,
    var attributes: MutableMap<String, String>,
    var amount: Double,
    var unit: String,
    var status: IncidentManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentManifestStatus): Boolean {
        when (status) {
            IncidentManifestStatus.PROPOSED -> return next == IncidentManifestStatus.WITHDRAWN
            IncidentManifestStatus.APPROVED -> return next == IncidentManifestStatus.REJECTED || next == IncidentManifestStatus.WITHDRAWN
            IncidentManifestStatus.REJECTED -> return next == IncidentManifestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: IncidentManifestStatus) {
        if (!canMoveTo(next)) {
            error("incident manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((capacity.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): IncidentManifest {
        return IncidentManifest(
            code = code,
            title = title,
            limit = limit,
            depot = depot,
            capacity = capacity,
            attributes = attributes,
            amount = amount,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a incident manifest in status proposed. */
fun newIncidentManifest(code: String, title: String): IncidentManifest {
    return IncidentManifest(
        code = code,
        title = title,
        limit = 0,
        depot = "",
        capacity = 0,
        attributes = mutableMapOf<String, String>(),
        amount = 0.0,
        unit = "",
        status = IncidentManifestStatus.PROPOSED,
    )
}
