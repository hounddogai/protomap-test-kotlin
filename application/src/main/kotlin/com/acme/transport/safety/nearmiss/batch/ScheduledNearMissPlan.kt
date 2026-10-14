package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled near miss plan is in its life cycle. */
enum class ScheduledNearMissPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One scheduled near miss plan, with what the planners need to act on it. */
data class ScheduledNearMissPlan(
    var code: String,
    var label: String,
    var margin: Double,
    var routeCode: String,
    var etaMinutes: Int,
    var archived: Boolean,
    var status: ScheduledNearMissPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledNearMissPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledNearMissPlanStatus): Boolean {
        when (status) {
            ScheduledNearMissPlanStatus.PLANNED -> {
                when (next) {
                    ScheduledNearMissPlanStatus.IN_PROGRESS, ScheduledNearMissPlanStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledNearMissPlanStatus.SCHEDULED -> {
                when (next) {
                    ScheduledNearMissPlanStatus.IN_PROGRESS, ScheduledNearMissPlanStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledNearMissPlanStatus.IN_PROGRESS -> {
                when (next) {
                    ScheduledNearMissPlanStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledNearMissPlanStatus.COMPLETED -> return next == ScheduledNearMissPlanStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledNearMissPlanStatus) {
        if (!canMoveTo(next)) {
            error("scheduled near miss plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveLoad(): Double = round((margin - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): ScheduledNearMissPlan {
        return ScheduledNearMissPlan(
            code = code,
            label = label,
            margin = margin,
            routeCode = routeCode,
            etaMinutes = etaMinutes,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled near miss plan in status planned. */
fun newScheduledNearMissPlan(code: String, label: String): ScheduledNearMissPlan {
    return ScheduledNearMissPlan(
        code = code,
        label = label,
        margin = 0.0,
        routeCode = "",
        etaMinutes = 0,
        archived = false,
        status = ScheduledNearMissPlanStatus.PLANNED,
    )
}
