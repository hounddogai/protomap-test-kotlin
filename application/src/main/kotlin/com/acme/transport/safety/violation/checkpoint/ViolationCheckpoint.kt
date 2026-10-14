package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.text.normalize

// The violation checkpoint records of safety's violation work, and the rules, stores, and calculations around
// them.

/** Where a violation checkpoint is in its life cycle. */
enum class ViolationCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A violation checkpoint as the violation team keeps it. */
data class ViolationCheckpoint(
    var code: String,
    var title: String,
    var phone: String,
    var ceiling: Double,
    var segment: String,
    var routeCode: String,
    var depot: String,
    var vehicleId: String,
    var notes: String,
    var category: String,
    var status: ViolationCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationCheckpointStatus): Boolean {
        when (status) {
            ViolationCheckpointStatus.CREATED -> return next == ViolationCheckpointStatus.VALIDATED || next == ViolationCheckpointStatus.PUBLISHED
            ViolationCheckpointStatus.VALIDATED -> return next == ViolationCheckpointStatus.RETIRED
            ViolationCheckpointStatus.PUBLISHED -> {
                when (next) {
                    ViolationCheckpointStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("violation checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"violation checkpoint"} $code"

    fun measuredValue(): Double = ceiling

    fun snapshot(): ViolationCheckpoint {
        return ViolationCheckpoint(
            code = code,
            title = title,
            phone = phone,
            ceiling = ceiling,
            segment = segment,
            routeCode = routeCode,
            depot = depot,
            vehicleId = vehicleId,
            notes = notes,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a violation checkpoint in status created. */
fun newViolationCheckpoint(code: String, title: String): ViolationCheckpoint {
    return ViolationCheckpoint(
        code = code,
        title = title,
        phone = "",
        ceiling = 0.0,
        segment = "",
        routeCode = "",
        depot = "",
        vehicleId = "",
        notes = "",
        category = "",
        status = ViolationCheckpointStatus.CREATED,
    )
}
