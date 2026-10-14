package com.acme.transport.fleetops.utilization.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The utilization target records of fleetops's utilization work, and the rules, stores, and calculations
// around them.

/** Where a utilization target is in its life cycle. */
enum class UtilizationTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a utilization target and its current status. */
data class UtilizationTarget(
    var code: String,
    var title: String,
    var source: String,
    var margin: Double,
    var threshold: Double,
    var durationMinutes: Int,
    var floorValue: Double,
    var status: UtilizationTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationTargetStatus): Boolean {
        when (status) {
            UtilizationTargetStatus.PLANNED -> return next == UtilizationTargetStatus.IN_PROGRESS || next == UtilizationTargetStatus.CANCELLED
            UtilizationTargetStatus.SCHEDULED -> {
                when (next) {
                    UtilizationTargetStatus.IN_PROGRESS, UtilizationTargetStatus.COMPLETED -> return true
                }
                return false
            }
            UtilizationTargetStatus.IN_PROGRESS -> {
                when (next) {
                    UtilizationTargetStatus.CANCELLED -> return true
                }
                return false
            }
            UtilizationTargetStatus.COMPLETED -> return next == UtilizationTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: UtilizationTargetStatus) {
        if (!canMoveTo(next)) {
            error("utilization target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedTotal(): Double = round(margin * floorValue * 100) / 100

    fun snapshot(): UtilizationTarget {
        return UtilizationTarget(
            code = code,
            title = title,
            source = source,
            margin = margin,
            threshold = threshold,
            durationMinutes = durationMinutes,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a utilization target in status planned. */
fun newUtilizationTarget(code: String, title: String): UtilizationTarget {
    return UtilizationTarget(
        code = code,
        title = title,
        source = "",
        margin = 0.0,
        threshold = 0.0,
        durationMinutes = 0,
        floorValue = 0.0,
        status = UtilizationTargetStatus.PLANNED,
    )
}
