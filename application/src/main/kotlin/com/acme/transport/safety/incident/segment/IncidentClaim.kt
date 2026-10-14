package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident claim is in its life cycle. */
enum class IncidentClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A incident claim. Its code is unique within a tenant. */
data class IncidentClaim(
    var code: String,
    var label: String,
    var retries: Int,
    var loadKg: Double,
    var weight: Double,
    var attributes: MutableMap<String, String>,
    var depot: String,
    var status: IncidentClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentClaimStatus): Boolean {
        when (status) {
            IncidentClaimStatus.CREATED -> {
                when (next) {
                    IncidentClaimStatus.VALIDATED, IncidentClaimStatus.PUBLISHED -> return true
                }
                return false
            }
            IncidentClaimStatus.VALIDATED -> return next == IncidentClaimStatus.RETIRED
            IncidentClaimStatus.PUBLISHED -> {
                when (next) {
                    IncidentClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentClaimStatus) {
        if (!canMoveTo(next)) {
            error("incident claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedExposure(): Double = round((weight - retries.toDouble()) * 100) / 100

    fun snapshot(): IncidentClaim {
        return IncidentClaim(
            code = code,
            label = label,
            retries = retries,
            loadKg = loadKg,
            weight = weight,
            attributes = attributes,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a incident claim in status created. */
fun newIncidentClaim(code: String, label: String): IncidentClaim {
    return IncidentClaim(
        code = code,
        label = label,
        retries = 0,
        loadKg = 0.0,
        weight = 0.0,
        attributes = mutableMapOf<String, String>(),
        depot = "",
        status = IncidentClaimStatus.CREATED,
    )
}
