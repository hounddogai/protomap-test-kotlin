package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global capacity schedule is in its life cycle. */
enum class GlobalCapacityScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One global capacity schedule, with what the planners need to act on it. */
data class GlobalCapacitySchedule(
    var code: String,
    var label: String,
    var name: String,
    var attempts: Int,
    var budget: Double,
    var status: GlobalCapacityScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCapacityScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCapacityScheduleStatus): Boolean {
        when (status) {
            GlobalCapacityScheduleStatus.PROPOSED -> {
                when (next) {
                    GlobalCapacityScheduleStatus.REJECTED, GlobalCapacityScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalCapacityScheduleStatus.APPROVED -> {
                when (next) {
                    GlobalCapacityScheduleStatus.REJECTED, GlobalCapacityScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalCapacityScheduleStatus.REJECTED -> {
                when (next) {
                    GlobalCapacityScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCapacityScheduleStatus) {
        if (!canMoveTo(next)) {
            error("global capacity schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global capacity schedule"} $code"

    fun weightedLoad(): Double = round((budget + attempts.toDouble()) * 100) / 100

    fun snapshot(): GlobalCapacitySchedule {
        return GlobalCapacitySchedule(
            code = code,
            label = label,
            name = name,
            attempts = attempts,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global capacity schedule in status proposed. */
fun newGlobalCapacitySchedule(code: String, label: String): GlobalCapacitySchedule {
    return GlobalCapacitySchedule(
        code = code,
        label = label,
        name = "",
        attempts = 0,
        budget = 0.0,
        status = GlobalCapacityScheduleStatus.PROPOSED,
    )
}
