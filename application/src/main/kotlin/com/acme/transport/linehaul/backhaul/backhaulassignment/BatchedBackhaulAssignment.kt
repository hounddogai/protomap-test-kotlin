package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.text.normalize

// The batched backhaul assignment records of linehaul's backhaul work, and the rules, stores, and
// calculations around them.

/** Where a batched backhaul assignment is in its life cycle. */
enum class BatchedBackhaulAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One batched backhaul assignment, with what the planners need to act on it. */
data class BatchedBackhaulAssignment(
    var code: String,
    var title: String,
    var approved: Boolean,
    var trailerId: String,
    var country: String,
    var quantity: Int,
    var status: BatchedBackhaulAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedBackhaulAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedBackhaulAssignmentStatus): Boolean {
        when (status) {
            BatchedBackhaulAssignmentStatus.DRAFT -> {
                when (next) {
                    BatchedBackhaulAssignmentStatus.PENDING, BatchedBackhaulAssignmentStatus.CLOSED -> return true
                }
                return false
            }
            BatchedBackhaulAssignmentStatus.PENDING -> return next == BatchedBackhaulAssignmentStatus.ACTIVE || next == BatchedBackhaulAssignmentStatus.SUSPENDED
            BatchedBackhaulAssignmentStatus.ACTIVE -> {
                when (next) {
                    BatchedBackhaulAssignmentStatus.CLOSED -> return true
                }
                return false
            }
            BatchedBackhaulAssignmentStatus.SUSPENDED -> {
                when (next) {
                    BatchedBackhaulAssignmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedBackhaulAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("batched backhaul assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): BatchedBackhaulAssignment {
        return BatchedBackhaulAssignment(
            code = code,
            title = title,
            approved = approved,
            trailerId = trailerId,
            country = country,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched backhaul assignment in status draft. */
fun newBatchedBackhaulAssignment(code: String, title: String): BatchedBackhaulAssignment {
    return BatchedBackhaulAssignment(
        code = code,
        title = title,
        approved = false,
        trailerId = "",
        country = "",
        quantity = 0,
        status = BatchedBackhaulAssignmentStatus.DRAFT,
    )
}
