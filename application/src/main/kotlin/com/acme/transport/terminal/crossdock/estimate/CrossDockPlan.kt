package com.acme.transport.terminal.crossdock.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock plan is in its life cycle. */
enum class CrossDockPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A cross dock plan. Its code is unique within a tenant. */
data class CrossDockPlan(
    var code: String,
    var title: String,
    var maximum: Double,
    var odometer: Int,
    var amount: Double,
    var updatedAt: Int,
    var label: String,
    var dock: String,
    var graceDays: Int,
    var status: CrossDockPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockPlanStatus): Boolean {
        when (status) {
            CrossDockPlanStatus.PLANNED -> {
                when (next) {
                    CrossDockPlanStatus.IN_PROGRESS -> return true
                }
                return false
            }
            CrossDockPlanStatus.SCHEDULED -> {
                when (next) {
                    CrossDockPlanStatus.CANCELLED -> return true
                }
                return false
            }
            CrossDockPlanStatus.IN_PROGRESS -> return next == CrossDockPlanStatus.COMPLETED || next == CrossDockPlanStatus.CANCELLED
            CrossDockPlanStatus.COMPLETED -> return next == CrossDockPlanStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CrossDockPlanStatus) {
        if (!canMoveTo(next)) {
            error("cross dock plan $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $title"

    fun effectiveVolume(): Double = round(graceDays.toDouble() * amount * 100) / 100

    fun snapshot(): CrossDockPlan {
        return CrossDockPlan(
            code = code,
            title = title,
            maximum = maximum,
            odometer = odometer,
            amount = amount,
            updatedAt = updatedAt,
            label = label,
            dock = dock,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cross dock plan in status planned. */
fun newCrossDockPlan(code: String, title: String): CrossDockPlan {
    return CrossDockPlan(
        code = code,
        title = title,
        maximum = 0.0,
        odometer = 0,
        amount = 0.0,
        updatedAt = 0,
        label = "",
        dock = "",
        graceDays = 0,
        status = CrossDockPlanStatus.PLANNED,
    )
}
