package com.acme.transport.crewing.roster.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster estimate is in its life cycle. */
enum class RosterEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A roster estimate. Its code is unique within a tenant. */
data class RosterEstimate(
    var code: String,
    var label: String,
    var priority: Int,
    var score: Double,
    var odometer: Int,
    var tags: MutableList<String>,
    var durationMinutes: Int,
    var contactEmail: String,
    var status: RosterEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterEstimateStatus): Boolean {
        when (status) {
            RosterEstimateStatus.REQUESTED -> return next == RosterEstimateStatus.REVOKED || next == RosterEstimateStatus.EXPIRED
            RosterEstimateStatus.GRANTED -> return next == RosterEstimateStatus.REVOKED
            RosterEstimateStatus.REVOKED -> {
                when (next) {
                    RosterEstimateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterEstimateStatus) {
        if (!canMoveTo(next)) {
            error("roster estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"roster estimate"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedValue(): Double = round((score + odometer.toDouble()) * 100) / 100

    fun snapshot(): RosterEstimate {
        return RosterEstimate(
            code = code,
            label = label,
            priority = priority,
            score = score,
            odometer = odometer,
            tags = tags,
            durationMinutes = durationMinutes,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a roster estimate in status requested. */
fun newRosterEstimate(code: String, label: String): RosterEstimate {
    return RosterEstimate(
        code = code,
        label = label,
        priority = 0,
        score = 0.0,
        odometer = 0,
        tags = mutableListOf<String>(),
        durationMinutes = 0,
        contactEmail = "",
        status = RosterEstimateStatus.REQUESTED,
    )
}
