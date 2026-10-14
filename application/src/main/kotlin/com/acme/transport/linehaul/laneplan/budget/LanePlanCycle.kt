package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan cycle is in its life cycle. */
enum class LanePlanCycleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lane plan cycle as the lane plan team keeps it. */
data class LanePlanCycle(
    var code: String,
    var label: String,
    var channel: String,
    var sequence: Int,
    var fuelLiters: Double,
    var timezone: String,
    var source: String,
    var status: LanePlanCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanCycleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanCycleStatus): Boolean {
        when (status) {
            LanePlanCycleStatus.OPEN -> {
                when (next) {
                    LanePlanCycleStatus.IN_REVIEW, LanePlanCycleStatus.ARCHIVED -> return true
                }
                return false
            }
            LanePlanCycleStatus.IN_REVIEW -> {
                when (next) {
                    LanePlanCycleStatus.REOPENED -> return true
                }
                return false
            }
            LanePlanCycleStatus.RESOLVED -> return next == LanePlanCycleStatus.REOPENED || next == LanePlanCycleStatus.ARCHIVED
            LanePlanCycleStatus.REOPENED -> {
                when (next) {
                    LanePlanCycleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanCycleStatus) {
        if (!canMoveTo(next)) {
            error("lane plan cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netLoad(): Double = round((fuelLiters + sequence.toDouble()) * 100) / 100

    fun snapshot(): LanePlanCycle {
        return LanePlanCycle(
            code = code,
            label = label,
            channel = channel,
            sequence = sequence,
            fuelLiters = fuelLiters,
            timezone = timezone,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lane plan cycle in status open. */
fun newLanePlanCycle(code: String, label: String): LanePlanCycle {
    return LanePlanCycle(
        code = code,
        label = label,
        channel = "",
        sequence = 0,
        fuelLiters = 0.0,
        timezone = "",
        source = "",
        status = LanePlanCycleStatus.OPEN,
    )
}
