package com.acme.transport.terminal.sortation.plan

import com.acme.transport.kit.text.normalize

// The sortation plan records of terminal's sortation work, and the rules, stores, and calculations around
// them.

/** Where a sortation plan is in its life cycle. */
enum class SortationPlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One sortation plan, with what the planners need to act on it. */
data class SortationPlan(
    var code: String,
    var label: String,
    var currency: String,
    var lane: String,
    var checksum: String,
    var routeCode: String,
    var quantity: Int,
    var status: SortationPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationPlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationPlanStatus): Boolean {
        when (status) {
            SortationPlanStatus.NEW -> return next == SortationPlanStatus.RUNNING || next == SortationPlanStatus.DONE
            SortationPlanStatus.QUEUED -> {
                when (next) {
                    SortationPlanStatus.DONE, SortationPlanStatus.FAILED -> return true
                }
                return false
            }
            SortationPlanStatus.RUNNING -> {
                when (next) {
                    SortationPlanStatus.DONE, SortationPlanStatus.FAILED -> return true
                }
                return false
            }
            SortationPlanStatus.DONE -> {
                when (next) {
                    SortationPlanStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationPlanStatus) {
        if (!canMoveTo(next)) {
            error("sortation plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): SortationPlan {
        return SortationPlan(
            code = code,
            label = label,
            currency = currency,
            lane = lane,
            checksum = checksum,
            routeCode = routeCode,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a sortation plan in status new. */
fun newSortationPlan(code: String, label: String): SortationPlan {
    return SortationPlan(
        code = code,
        label = label,
        currency = "",
        lane = "",
        checksum = "",
        routeCode = "",
        quantity = 0,
        status = SortationPlanStatus.NEW,
    )
}
