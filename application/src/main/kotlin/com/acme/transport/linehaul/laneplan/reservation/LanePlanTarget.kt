package com.acme.transport.linehaul.laneplan.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan target is in its life cycle. */
enum class LanePlanTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a lane plan target and its current status. */
data class LanePlanTarget(
    var code: String,
    var title: String,
    var budget: Double,
    var summary: String,
    var windowDays: Int,
    var ratio: Double,
    var status: LanePlanTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanTargetStatus): Boolean {
        when (status) {
            LanePlanTargetStatus.DRAFT -> {
                when (next) {
                    LanePlanTargetStatus.ACTIVE -> return true
                }
                return false
            }
            LanePlanTargetStatus.PENDING -> return next == LanePlanTargetStatus.ACTIVE
            LanePlanTargetStatus.ACTIVE -> {
                when (next) {
                    LanePlanTargetStatus.SUSPENDED -> return true
                }
                return false
            }
            LanePlanTargetStatus.SUSPENDED -> return next == LanePlanTargetStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LanePlanTargetStatus) {
        if (!canMoveTo(next)) {
            error("lane plan target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveTotal(): Double = round((ratio + budget) * 100) / 100

    fun snapshot(): LanePlanTarget {
        return LanePlanTarget(
            code = code,
            title = title,
            budget = budget,
            summary = summary,
            windowDays = windowDays,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lane plan target in status draft. */
fun newLanePlanTarget(code: String, title: String): LanePlanTarget {
    return LanePlanTarget(
        code = code,
        title = title,
        budget = 0.0,
        summary = "",
        windowDays = 0,
        ratio = 0.0,
        status = LanePlanTargetStatus.DRAFT,
    )
}
