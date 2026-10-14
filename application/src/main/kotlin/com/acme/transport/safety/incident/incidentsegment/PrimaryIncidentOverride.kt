package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary incident override is in its life cycle. */
enum class PrimaryIncidentOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary incident override. Its code is unique within a tenant. */
data class PrimaryIncidentOverride(
    var code: String,
    var title: String,
    var ceiling: Double,
    var tags: MutableList<String>,
    var etaMinutes: Int,
    var status: PrimaryIncidentOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryIncidentOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryIncidentOverrideStatus): Boolean {
        when (status) {
            PrimaryIncidentOverrideStatus.PROPOSED -> {
                when (next) {
                    PrimaryIncidentOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryIncidentOverrideStatus.APPROVED -> {
                when (next) {
                    PrimaryIncidentOverrideStatus.REJECTED -> return true
                }
                return false
            }
            PrimaryIncidentOverrideStatus.REJECTED -> return next == PrimaryIncidentOverrideStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryIncidentOverrideStatus) {
        if (!canMoveTo(next)) {
            error("primary incident override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveExposure(): Double = round(ceiling * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): PrimaryIncidentOverride {
        return PrimaryIncidentOverride(
            code = code,
            title = title,
            ceiling = ceiling,
            tags = tags,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary incident override in status proposed. */
fun newPrimaryIncidentOverride(code: String, title: String): PrimaryIncidentOverride {
    return PrimaryIncidentOverride(
        code = code,
        title = title,
        ceiling = 0.0,
        tags = mutableListOf<String>(),
        etaMinutes = 0,
        status = PrimaryIncidentOverrideStatus.PROPOSED,
    )
}
