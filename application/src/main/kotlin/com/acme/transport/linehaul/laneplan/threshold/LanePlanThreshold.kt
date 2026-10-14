package com.acme.transport.linehaul.laneplan.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lane plan threshold records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a lane plan threshold is in its life cycle. */
enum class LanePlanThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lane plan threshold as the lane plan team keeps it. */
data class LanePlanThreshold(
    var code: String,
    var title: String,
    var routeCode: String,
    var durationMinutes: Int,
    var capacity: Int,
    var odometer: Int,
    var active: Boolean,
    var locale: String,
    var revision: Int,
    var rate: Double,
    var status: LanePlanThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanThresholdStatus): Boolean {
        when (status) {
            LanePlanThresholdStatus.OPEN -> {
                when (next) {
                    LanePlanThresholdStatus.IN_REVIEW -> return true
                }
                return false
            }
            LanePlanThresholdStatus.IN_REVIEW -> {
                when (next) {
                    LanePlanThresholdStatus.REOPENED, LanePlanThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            LanePlanThresholdStatus.RESOLVED -> {
                when (next) {
                    LanePlanThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            LanePlanThresholdStatus.REOPENED -> {
                when (next) {
                    LanePlanThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanThresholdStatus) {
        if (!canMoveTo(next)) {
            error("lane plan threshold $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun projectedTotal(): Double = round((capacity.toDouble() - rate) * 100) / 100

    fun snapshot(): LanePlanThreshold {
        return LanePlanThreshold(
            code = code,
            title = title,
            routeCode = routeCode,
            durationMinutes = durationMinutes,
            capacity = capacity,
            odometer = odometer,
            active = active,
            locale = locale,
            revision = revision,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lane plan threshold in status open. */
fun newLanePlanThreshold(code: String, title: String): LanePlanThreshold {
    return LanePlanThreshold(
        code = code,
        title = title,
        routeCode = "",
        durationMinutes = 0,
        capacity = 0,
        odometer = 0,
        active = false,
        locale = "",
        revision = 0,
        rate = 0.0,
        status = LanePlanThresholdStatus.OPEN,
    )
}
