package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.kit.text.normalize

/** Where a regional incident manifest is in its life cycle. */
enum class RegionalIncidentManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A regional incident manifest as the incident team keeps it. */
data class RegionalIncidentManifest(
    var code: String,
    var label: String,
    var name: String,
    var limit: Int,
    var driverId: String,
    var status: RegionalIncidentManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalIncidentManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalIncidentManifestStatus): Boolean {
        when (status) {
            RegionalIncidentManifestStatus.PROPOSED -> {
                when (next) {
                    RegionalIncidentManifestStatus.REJECTED, RegionalIncidentManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalIncidentManifestStatus.APPROVED -> return next == RegionalIncidentManifestStatus.REJECTED || next == RegionalIncidentManifestStatus.WITHDRAWN
            RegionalIncidentManifestStatus.REJECTED -> {
                when (next) {
                    RegionalIncidentManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalIncidentManifestStatus) {
        if (!canMoveTo(next)) {
            error("regional incident manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): RegionalIncidentManifest {
        return RegionalIncidentManifest(
            code = code,
            label = label,
            name = name,
            limit = limit,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional incident manifest in status proposed. */
fun newRegionalIncidentManifest(code: String, label: String): RegionalIncidentManifest {
    return RegionalIncidentManifest(
        code = code,
        label = label,
        name = "",
        limit = 0,
        driverId = "",
        status = RegionalIncidentManifestStatus.PROPOSED,
    )
}
