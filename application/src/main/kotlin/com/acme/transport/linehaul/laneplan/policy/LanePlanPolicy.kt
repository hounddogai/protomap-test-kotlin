package com.acme.transport.linehaul.laneplan.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lane plan policy records of linehaul's lane plan work, and the rules, stores, and calculations around
// them.

/** Where a lane plan policy is in its life cycle. */
enum class LanePlanPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a lane plan policy and its current status. */
data class LanePlanPolicy(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var margin: Double,
    var timezone: String,
    var minimum: Double,
    var status: LanePlanPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanPolicyStatus): Boolean {
        when (status) {
            LanePlanPolicyStatus.PLANNED -> return next == LanePlanPolicyStatus.IN_PROGRESS
            LanePlanPolicyStatus.SCHEDULED -> {
                when (next) {
                    LanePlanPolicyStatus.COMPLETED -> return true
                }
                return false
            }
            LanePlanPolicyStatus.IN_PROGRESS -> return next == LanePlanPolicyStatus.CANCELLED
            LanePlanPolicyStatus.COMPLETED -> {
                when (next) {
                    LanePlanPolicyStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanPolicyStatus) {
        if (!canMoveTo(next)) {
            error("lane plan policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round(minimum * distanceKm * 100) / 100

    fun snapshot(): LanePlanPolicy {
        return LanePlanPolicy(
            code = code,
            name = name,
            distanceKm = distanceKm,
            margin = margin,
            timezone = timezone,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lane plan policy in status planned. */
fun newLanePlanPolicy(code: String, name: String): LanePlanPolicy {
    return LanePlanPolicy(
        code = code,
        name = name,
        distanceKm = 0.0,
        margin = 0.0,
        timezone = "",
        minimum = 0.0,
        status = LanePlanPolicyStatus.PLANNED,
    )
}
