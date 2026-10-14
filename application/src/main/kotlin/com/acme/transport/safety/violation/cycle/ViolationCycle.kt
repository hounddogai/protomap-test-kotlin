package com.acme.transport.safety.violation.cycle

import com.acme.transport.kit.text.normalize

// The violation cycle records of safety's violation work, and the rules, stores, and calculations around
// them.

/** Where a violation cycle is in its life cycle. */
enum class ViolationCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A violation cycle as the violation team keeps it. */
data class ViolationCycle(
    var code: String,
    var name: String,
    var windowDays: Int,
    var active: Boolean,
    var archived: Boolean,
    var status: ViolationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationCycleStatus): Boolean {
        when (status) {
            ViolationCycleStatus.CREATED -> {
                when (next) {
                    ViolationCycleStatus.PUBLISHED, ViolationCycleStatus.RETIRED -> return true
                }
                return false
            }
            ViolationCycleStatus.VALIDATED -> {
                when (next) {
                    ViolationCycleStatus.PUBLISHED -> return true
                }
                return false
            }
            ViolationCycleStatus.PUBLISHED -> return next == ViolationCycleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ViolationCycleStatus) {
        if (!canMoveTo(next)) {
            error("violation cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): ViolationCycle {
        return ViolationCycle(
            code = code,
            name = name,
            windowDays = windowDays,
            active = active,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a violation cycle in status created. */
fun newViolationCycle(code: String, name: String): ViolationCycle {
    return ViolationCycle(
        code = code,
        name = name,
        windowDays = 0,
        active = false,
        archived = false,
        status = ViolationCycleStatus.CREATED,
    )
}
