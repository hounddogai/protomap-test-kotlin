package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.text.normalize

/** Where a weighted violation cycle is in its life cycle. */
enum class WeightedViolationCycleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted violation cycle. Its code is unique within a tenant. */
data class WeightedViolationCycle(
    var code: String,
    var name: String,
    var visible: Boolean,
    var depot: String,
    var batchSize: Int,
    var status: WeightedViolationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedViolationCycleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedViolationCycleStatus): Boolean {
        when (status) {
            WeightedViolationCycleStatus.NEW -> return next == WeightedViolationCycleStatus.RUNNING || next == WeightedViolationCycleStatus.FAILED
            WeightedViolationCycleStatus.QUEUED -> {
                when (next) {
                    WeightedViolationCycleStatus.RUNNING, WeightedViolationCycleStatus.FAILED -> return true
                }
                return false
            }
            WeightedViolationCycleStatus.RUNNING -> return next == WeightedViolationCycleStatus.FAILED
            WeightedViolationCycleStatus.DONE -> {
                when (next) {
                    WeightedViolationCycleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedViolationCycleStatus) {
        if (!canMoveTo(next)) {
            error("weighted violation cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): WeightedViolationCycle {
        return WeightedViolationCycle(
            code = code,
            name = name,
            visible = visible,
            depot = depot,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a weighted violation cycle in status new. */
fun newWeightedViolationCycle(code: String, name: String): WeightedViolationCycle {
    return WeightedViolationCycle(
        code = code,
        name = name,
        visible = false,
        depot = "",
        batchSize = 0,
        status = WeightedViolationCycleStatus.NEW,
    )
}
