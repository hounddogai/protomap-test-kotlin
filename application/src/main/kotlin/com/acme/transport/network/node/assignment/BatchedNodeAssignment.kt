package com.acme.transport.network.node.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched node assignment records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a batched node assignment is in its life cycle. */
enum class BatchedNodeAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched node assignment as the node team keeps it. */
data class BatchedNodeAssignment(
    var code: String,
    var title: String,
    var contactEmail: String,
    var rate: Double,
    var distanceKm: Double,
    var status: BatchedNodeAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedNodeAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedNodeAssignmentStatus): Boolean {
        when (status) {
            BatchedNodeAssignmentStatus.CREATED -> {
                when (next) {
                    BatchedNodeAssignmentStatus.PUBLISHED, BatchedNodeAssignmentStatus.RETIRED -> return true
                }
                return false
            }
            BatchedNodeAssignmentStatus.VALIDATED -> {
                when (next) {
                    BatchedNodeAssignmentStatus.PUBLISHED, BatchedNodeAssignmentStatus.RETIRED -> return true
                }
                return false
            }
            BatchedNodeAssignmentStatus.PUBLISHED -> {
                when (next) {
                    BatchedNodeAssignmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedNodeAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("batched node assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((distanceKm + rate) * 100) / 100

    fun snapshot(): BatchedNodeAssignment {
        return BatchedNodeAssignment(
            code = code,
            title = title,
            contactEmail = contactEmail,
            rate = rate,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched node assignment in status created. */
fun newBatchedNodeAssignment(code: String, title: String): BatchedNodeAssignment {
    return BatchedNodeAssignment(
        code = code,
        title = title,
        contactEmail = "",
        rate = 0.0,
        distanceKm = 0.0,
        status = BatchedNodeAssignmentStatus.CREATED,
    )
}
