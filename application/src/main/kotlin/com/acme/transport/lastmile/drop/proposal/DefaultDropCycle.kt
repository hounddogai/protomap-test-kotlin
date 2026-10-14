package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.text.normalize

/** Where a default drop cycle is in its life cycle. */
enum class DefaultDropCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One default drop cycle, with what the planners need to act on it. */
data class DefaultDropCycle(
    var code: String,
    var label: String,
    var ratio: Double,
    var trailerId: String,
    var shiftCode: String,
    var status: DefaultDropCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDropCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDropCycleStatus): Boolean {
        when (status) {
            DefaultDropCycleStatus.CREATED -> {
                when (next) {
                    DefaultDropCycleStatus.PUBLISHED, DefaultDropCycleStatus.RETIRED -> return true
                }
                return false
            }
            DefaultDropCycleStatus.VALIDATED -> return next == DefaultDropCycleStatus.PUBLISHED || next == DefaultDropCycleStatus.RETIRED
            DefaultDropCycleStatus.PUBLISHED -> return next == DefaultDropCycleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultDropCycleStatus) {
        if (!canMoveTo(next)) {
            error("default drop cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default drop cycle"} $code"

    fun measuredValue(): Double = ratio

    fun snapshot(): DefaultDropCycle {
        return DefaultDropCycle(
            code = code,
            label = label,
            ratio = ratio,
            trailerId = trailerId,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default drop cycle in status created. */
fun newDefaultDropCycle(code: String, label: String): DefaultDropCycle {
    return DefaultDropCycle(
        code = code,
        label = label,
        ratio = 0.0,
        trailerId = "",
        shiftCode = "",
        status = DefaultDropCycleStatus.CREATED,
    )
}
