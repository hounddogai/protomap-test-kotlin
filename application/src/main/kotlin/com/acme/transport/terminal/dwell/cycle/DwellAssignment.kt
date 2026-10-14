package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell assignment is in its life cycle. */
enum class DwellAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One dwell assignment, with what the planners need to act on it. */
data class DwellAssignment(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var minimum: Double,
    var odometer: Int,
    var pinned: Boolean,
    var discount: Double,
    var version: Int,
    var depot: String,
    var weight: Double,
    var status: DwellAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellAssignmentStatus): Boolean {
        when (status) {
            DwellAssignmentStatus.NEW -> return next == DwellAssignmentStatus.RUNNING || next == DwellAssignmentStatus.FAILED
            DwellAssignmentStatus.QUEUED -> return next == DwellAssignmentStatus.RUNNING || next == DwellAssignmentStatus.DONE
            DwellAssignmentStatus.RUNNING -> {
                when (next) {
                    DwellAssignmentStatus.FAILED -> return true
                }
                return false
            }
            DwellAssignmentStatus.DONE -> return next == DwellAssignmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DwellAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("dwell assignment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round((weight - discount) * 100) / 100

    fun snapshot(): DwellAssignment {
        return DwellAssignment(
            code = code,
            label = label,
            tags = tags,
            minimum = minimum,
            odometer = odometer,
            pinned = pinned,
            discount = discount,
            version = version,
            depot = depot,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a dwell assignment in status new. */
fun newDwellAssignment(code: String, label: String): DwellAssignment {
    return DwellAssignment(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        minimum = 0.0,
        odometer = 0,
        pinned = false,
        discount = 0.0,
        version = 0,
        depot = "",
        weight = 0.0,
        status = DwellAssignmentStatus.NEW,
    )
}
