package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled lane plan override is in its life cycle. */
enum class ScheduledLanePlanOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a scheduled lane plan override and its current status. */
data class ScheduledLanePlanOverride(
    var code: String,
    var name: String,
    var margin: Double,
    var unit: String,
    var weight: Double,
    var status: ScheduledLanePlanOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLanePlanOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLanePlanOverrideStatus): Boolean {
        when (status) {
            ScheduledLanePlanOverrideStatus.PROPOSED -> {
                when (next) {
                    ScheduledLanePlanOverrideStatus.APPROVED -> return true
                }
                return false
            }
            ScheduledLanePlanOverrideStatus.APPROVED -> {
                when (next) {
                    ScheduledLanePlanOverrideStatus.REJECTED, ScheduledLanePlanOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScheduledLanePlanOverrideStatus.REJECTED -> {
                when (next) {
                    ScheduledLanePlanOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledLanePlanOverrideStatus) {
        if (!canMoveTo(next)) {
            error("scheduled lane plan override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedValue(): Double = round(weight * margin * 100) / 100

    fun snapshot(): ScheduledLanePlanOverride {
        return ScheduledLanePlanOverride(
            code = code,
            name = name,
            margin = margin,
            unit = unit,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled lane plan override in status proposed. */
fun newScheduledLanePlanOverride(code: String, name: String): ScheduledLanePlanOverride {
    return ScheduledLanePlanOverride(
        code = code,
        name = name,
        margin = 0.0,
        unit = "",
        weight = 0.0,
        status = ScheduledLanePlanOverrideStatus.PROPOSED,
    )
}
