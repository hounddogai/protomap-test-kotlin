package com.acme.transport.linehaul.laneplan.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan override is in its life cycle. */
enum class LanePlanOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lane plan override as the lane plan team keeps it. */
data class LanePlanOverride(
    var code: String,
    var label: String,
    var loadKg: Double,
    var description: String,
    var graceDays: Int,
    var status: LanePlanOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanOverrideStatus): Boolean {
        when (status) {
            LanePlanOverrideStatus.DRAFT -> return next == LanePlanOverrideStatus.ACTIVE || next == LanePlanOverrideStatus.SUSPENDED
            LanePlanOverrideStatus.PENDING -> return next == LanePlanOverrideStatus.SUSPENDED
            LanePlanOverrideStatus.ACTIVE -> {
                when (next) {
                    LanePlanOverrideStatus.SUSPENDED, LanePlanOverrideStatus.CLOSED -> return true
                }
                return false
            }
            LanePlanOverrideStatus.SUSPENDED -> return next == LanePlanOverrideStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LanePlanOverrideStatus) {
        if (!canMoveTo(next)) {
            error("lane plan override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedValue(): Double = round((loadKg + graceDays.toDouble()) * 100) / 100

    fun snapshot(): LanePlanOverride {
        return LanePlanOverride(
            code = code,
            label = label,
            loadKg = loadKg,
            description = description,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lane plan override in status draft. */
fun newLanePlanOverride(code: String, label: String): LanePlanOverride {
    return LanePlanOverride(
        code = code,
        label = label,
        loadKg = 0.0,
        description = "",
        graceDays = 0,
        status = LanePlanOverrideStatus.DRAFT,
    )
}
