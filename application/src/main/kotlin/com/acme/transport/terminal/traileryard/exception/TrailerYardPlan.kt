package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.kit.text.normalize

/** Where a trailer yard plan is in its life cycle. */
enum class TrailerYardPlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A trailer yard plan. Its code is unique within a tenant. */
data class TrailerYardPlan(
    var code: String,
    var label: String,
    var startsAt: Int,
    var dock: String,
    var locale: String,
    var status: TrailerYardPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardPlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardPlanStatus): Boolean {
        when (status) {
            TrailerYardPlanStatus.NEW -> return next == TrailerYardPlanStatus.FAILED
            TrailerYardPlanStatus.QUEUED -> {
                when (next) {
                    TrailerYardPlanStatus.RUNNING, TrailerYardPlanStatus.DONE -> return true
                }
                return false
            }
            TrailerYardPlanStatus.RUNNING -> return next == TrailerYardPlanStatus.DONE || next == TrailerYardPlanStatus.FAILED
            TrailerYardPlanStatus.DONE -> return next == TrailerYardPlanStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TrailerYardPlanStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard plan $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"trailer yard plan"} $code"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): TrailerYardPlan {
        return TrailerYardPlan(
            code = code,
            label = label,
            startsAt = startsAt,
            dock = dock,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a trailer yard plan in status new. */
fun newTrailerYardPlan(code: String, label: String): TrailerYardPlan {
    return TrailerYardPlan(
        code = code,
        label = label,
        startsAt = 0,
        dock = "",
        locale = "",
        status = TrailerYardPlanStatus.NEW,
    )
}
