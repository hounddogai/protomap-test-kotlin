package com.acme.transport.crewing.qualification.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched qualification assignment is in its life cycle. */
enum class BatchedQualificationAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One batched qualification assignment, with what the planners need to act on it. */
data class BatchedQualificationAssignment(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var timezone: String,
    var distanceKm: Double,
    var status: BatchedQualificationAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedQualificationAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedQualificationAssignmentStatus): Boolean {
        when (status) {
            BatchedQualificationAssignmentStatus.PROPOSED -> return next == BatchedQualificationAssignmentStatus.WITHDRAWN
            BatchedQualificationAssignmentStatus.APPROVED -> {
                when (next) {
                    BatchedQualificationAssignmentStatus.REJECTED, BatchedQualificationAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedQualificationAssignmentStatus.REJECTED -> return next == BatchedQualificationAssignmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedQualificationAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("batched qualification assignment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun projectedLoad(): Double = round(expiresAt.toDouble() * distanceKm * 100) / 100

    fun snapshot(): BatchedQualificationAssignment {
        return BatchedQualificationAssignment(
            code = code,
            name = name,
            expiresAt = expiresAt,
            timezone = timezone,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched qualification assignment in status proposed. */
fun newBatchedQualificationAssignment(code: String, name: String): BatchedQualificationAssignment {
    return BatchedQualificationAssignment(
        code = code,
        name = name,
        expiresAt = 0,
        timezone = "",
        distanceKm = 0.0,
        status = BatchedQualificationAssignmentStatus.PROPOSED,
    )
}
