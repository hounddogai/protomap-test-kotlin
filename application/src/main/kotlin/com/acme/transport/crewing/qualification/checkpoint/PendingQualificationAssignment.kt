package com.acme.transport.crewing.qualification.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending qualification assignment is in its life cycle. */
enum class PendingQualificationAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A pending qualification assignment. Its code is unique within a tenant. */
data class PendingQualificationAssignment(
    var code: String,
    var label: String,
    var graceDays: Int,
    var ratio: Double,
    var carrier: String,
    var externalRef: String,
    var checksum: String,
    var status: PendingQualificationAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingQualificationAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingQualificationAssignmentStatus): Boolean {
        when (status) {
            PendingQualificationAssignmentStatus.PROPOSED -> return next == PendingQualificationAssignmentStatus.APPROVED
            PendingQualificationAssignmentStatus.APPROVED -> {
                when (next) {
                    PendingQualificationAssignmentStatus.REJECTED, PendingQualificationAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingQualificationAssignmentStatus.REJECTED -> {
                when (next) {
                    PendingQualificationAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingQualificationAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("pending qualification assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedVolume(): Double = round((graceDays.toDouble() + ratio) * 100) / 100

    fun snapshot(): PendingQualificationAssignment {
        return PendingQualificationAssignment(
            code = code,
            label = label,
            graceDays = graceDays,
            ratio = ratio,
            carrier = carrier,
            externalRef = externalRef,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending qualification assignment in status proposed. */
fun newPendingQualificationAssignment(code: String, label: String): PendingQualificationAssignment {
    return PendingQualificationAssignment(
        code = code,
        label = label,
        graceDays = 0,
        ratio = 0.0,
        carrier = "",
        externalRef = "",
        checksum = "",
        status = PendingQualificationAssignmentStatus.PROPOSED,
    )
}
