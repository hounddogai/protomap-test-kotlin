package com.acme.transport.terminal.dwell.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell plan is in its life cycle. */
enum class DwellPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One dwell plan, with what the planners need to act on it. */
data class DwellPlan(
    var code: String,
    var title: String,
    var quantity: Int,
    var rate: Double,
    var updatedAt: Int,
    var pinned: Boolean,
    var cost: Double,
    var etaMinutes: Int,
    var status: DwellPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellPlanStatus): Boolean {
        when (status) {
            DwellPlanStatus.CREATED -> {
                when (next) {
                    DwellPlanStatus.VALIDATED -> return true
                }
                return false
            }
            DwellPlanStatus.VALIDATED -> return next == DwellPlanStatus.PUBLISHED
            DwellPlanStatus.PUBLISHED -> {
                when (next) {
                    DwellPlanStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellPlanStatus) {
        if (!canMoveTo(next)) {
            error("dwell plan $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"dwell plan"} $code"

    fun effectiveTotal(): Double = round((updatedAt.toDouble() + rate) * 100) / 100

    fun snapshot(): DwellPlan {
        return DwellPlan(
            code = code,
            title = title,
            quantity = quantity,
            rate = rate,
            updatedAt = updatedAt,
            pinned = pinned,
            cost = cost,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dwell plan in status created. */
fun newDwellPlan(code: String, title: String): DwellPlan {
    return DwellPlan(
        code = code,
        title = title,
        quantity = 0,
        rate = 0.0,
        updatedAt = 0,
        pinned = false,
        cost = 0.0,
        etaMinutes = 0,
        status = DwellPlanStatus.CREATED,
    )
}
