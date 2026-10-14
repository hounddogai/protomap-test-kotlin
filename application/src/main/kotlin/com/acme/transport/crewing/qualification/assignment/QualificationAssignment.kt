package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification assignment records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a qualification assignment is in its life cycle. */
enum class QualificationAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A qualification assignment. Its code is unique within a tenant. */
data class QualificationAssignment(
    var code: String,
    var title: String,
    var checksum: String,
    var capacity: Int,
    var windowDays: Int,
    var updatedAt: Int,
    var loadKg: Double,
    var sequence: Int,
    var limit: Int,
    var amount: Double,
    var status: QualificationAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationAssignmentStatus): Boolean {
        when (status) {
            QualificationAssignmentStatus.OPEN -> {
                when (next) {
                    QualificationAssignmentStatus.IN_REVIEW -> return true
                }
                return false
            }
            QualificationAssignmentStatus.IN_REVIEW -> return next == QualificationAssignmentStatus.RESOLVED
            QualificationAssignmentStatus.RESOLVED -> {
                when (next) {
                    QualificationAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            QualificationAssignmentStatus.REOPENED -> return next == QualificationAssignmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: QualificationAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("qualification assignment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedExposure(): Double = round((amount + loadKg) * 100) / 100

    fun snapshot(): QualificationAssignment {
        return QualificationAssignment(
            code = code,
            title = title,
            checksum = checksum,
            capacity = capacity,
            windowDays = windowDays,
            updatedAt = updatedAt,
            loadKg = loadKg,
            sequence = sequence,
            limit = limit,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a qualification assignment in status open. */
fun newQualificationAssignment(code: String, title: String): QualificationAssignment {
    return QualificationAssignment(
        code = code,
        title = title,
        checksum = "",
        capacity = 0,
        windowDays = 0,
        updatedAt = 0,
        loadKg = 0.0,
        sequence = 0,
        limit = 0,
        amount = 0.0,
        status = QualificationAssignmentStatus.OPEN,
    )
}
