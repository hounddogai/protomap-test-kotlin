package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident profile is in its life cycle. */
enum class IncidentProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A incident profile. Its code is unique within a tenant. */
data class IncidentProfile(
    var code: String,
    var label: String,
    var lane: String,
    var threshold: Double,
    var amount: Double,
    var unit: String,
    var status: IncidentProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentProfileStatus): Boolean {
        when (status) {
            IncidentProfileStatus.OPEN -> return next == IncidentProfileStatus.IN_REVIEW
            IncidentProfileStatus.IN_REVIEW -> return next == IncidentProfileStatus.REOPENED || next == IncidentProfileStatus.ARCHIVED
            IncidentProfileStatus.RESOLVED -> {
                when (next) {
                    IncidentProfileStatus.REOPENED, IncidentProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            IncidentProfileStatus.REOPENED -> {
                when (next) {
                    IncidentProfileStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentProfileStatus) {
        if (!canMoveTo(next)) {
            error("incident profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveExposure(): Double = round((threshold - amount) * 100) / 100

    fun snapshot(): IncidentProfile {
        return IncidentProfile(
            code = code,
            label = label,
            lane = lane,
            threshold = threshold,
            amount = amount,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a incident profile in status open. */
fun newIncidentProfile(code: String, label: String): IncidentProfile {
    return IncidentProfile(
        code = code,
        label = label,
        lane = "",
        threshold = 0.0,
        amount = 0.0,
        unit = "",
        status = IncidentProfileStatus.OPEN,
    )
}
