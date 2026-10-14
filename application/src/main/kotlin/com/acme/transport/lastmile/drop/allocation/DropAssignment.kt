package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop assignment is in its life cycle. */
enum class DropAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A drop assignment as the drop team keeps it. */
data class DropAssignment(
    var code: String,
    var label: String,
    var archived: Boolean,
    var vehicleId: String,
    var category: String,
    var capacity: Int,
    var name: String,
    var revision: Int,
    var createdAt: Int,
    var description: String,
    var stopCount: Int,
    var status: DropAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropAssignmentStatus): Boolean {
        when (status) {
            DropAssignmentStatus.REQUESTED -> return next == DropAssignmentStatus.GRANTED || next == DropAssignmentStatus.EXPIRED
            DropAssignmentStatus.GRANTED -> {
                when (next) {
                    DropAssignmentStatus.REVOKED, DropAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            DropAssignmentStatus.REVOKED -> {
                when (next) {
                    DropAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("drop assignment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveValue(): Double = round((stopCount.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): DropAssignment {
        return DropAssignment(
            code = code,
            label = label,
            archived = archived,
            vehicleId = vehicleId,
            category = category,
            capacity = capacity,
            name = name,
            revision = revision,
            createdAt = createdAt,
            description = description,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a drop assignment in status requested. */
fun newDropAssignment(code: String, label: String): DropAssignment {
    return DropAssignment(
        code = code,
        label = label,
        archived = false,
        vehicleId = "",
        category = "",
        capacity = 0,
        name = "",
        revision = 0,
        createdAt = 0,
        description = "",
        stopCount = 0,
        status = DropAssignmentStatus.REQUESTED,
    )
}
