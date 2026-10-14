package com.acme.transport.network.servicearea.cappedareaassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict service area assignment records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a strict service area assignment is in its life cycle. */
enum class StrictServiceAreaAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict service area assignment as the service area team keeps it. */
data class StrictServiceAreaAssignment(
    var code: String,
    var name: String,
    var carrier: String,
    var stopCount: Int,
    var capacity: Int,
    var status: StrictServiceAreaAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictServiceAreaAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictServiceAreaAssignmentStatus): Boolean {
        when (status) {
            StrictServiceAreaAssignmentStatus.NEW -> return next == StrictServiceAreaAssignmentStatus.RUNNING || next == StrictServiceAreaAssignmentStatus.DONE
            StrictServiceAreaAssignmentStatus.QUEUED -> return next == StrictServiceAreaAssignmentStatus.DONE
            StrictServiceAreaAssignmentStatus.RUNNING -> {
                when (next) {
                    StrictServiceAreaAssignmentStatus.DONE, StrictServiceAreaAssignmentStatus.FAILED -> return true
                }
                return false
            }
            StrictServiceAreaAssignmentStatus.DONE -> {
                when (next) {
                    StrictServiceAreaAssignmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictServiceAreaAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("strict service area assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedValue(): Double = round(stopCount.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): StrictServiceAreaAssignment {
        return StrictServiceAreaAssignment(
            code = code,
            name = name,
            carrier = carrier,
            stopCount = stopCount,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict service area assignment in status new. */
fun newStrictServiceAreaAssignment(code: String, name: String): StrictServiceAreaAssignment {
    return StrictServiceAreaAssignment(
        code = code,
        name = name,
        carrier = "",
        stopCount = 0,
        capacity = 0,
        status = StrictServiceAreaAssignmentStatus.NEW,
    )
}
