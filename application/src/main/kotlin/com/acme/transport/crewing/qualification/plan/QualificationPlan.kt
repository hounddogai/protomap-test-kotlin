package com.acme.transport.crewing.qualification.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification plan records of crewing's qualification work, and the rules, stores, and calculations
// around them.

/** Where a qualification plan is in its life cycle. */
enum class QualificationPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A qualification plan as the qualification team keeps it. */
data class QualificationPlan(
    var code: String,
    var label: String,
    var priority: Int,
    var cost: Double,
    var ceiling: Double,
    var sequence: Int,
    var notes: String,
    var retries: Int,
    var channel: String,
    var depot: String,
    var status: QualificationPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationPlanStatus): Boolean {
        when (status) {
            QualificationPlanStatus.OPEN -> {
                when (next) {
                    QualificationPlanStatus.ARCHIVED -> return true
                }
                return false
            }
            QualificationPlanStatus.IN_REVIEW -> {
                when (next) {
                    QualificationPlanStatus.RESOLVED, QualificationPlanStatus.REOPENED -> return true
                }
                return false
            }
            QualificationPlanStatus.RESOLVED -> return next == QualificationPlanStatus.REOPENED
            QualificationPlanStatus.REOPENED -> return next == QualificationPlanStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: QualificationPlanStatus) {
        if (!canMoveTo(next)) {
            error("qualification plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveExposure(): Double = round((retries.toDouble() - ceiling) * 100) / 100

    fun snapshot(): QualificationPlan {
        return QualificationPlan(
            code = code,
            label = label,
            priority = priority,
            cost = cost,
            ceiling = ceiling,
            sequence = sequence,
            notes = notes,
            retries = retries,
            channel = channel,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a qualification plan in status open. */
fun newQualificationPlan(code: String, label: String): QualificationPlan {
    return QualificationPlan(
        code = code,
        label = label,
        priority = 0,
        cost = 0.0,
        ceiling = 0.0,
        sequence = 0,
        notes = "",
        retries = 0,
        channel = "",
        depot = "",
        status = QualificationPlanStatus.OPEN,
    )
}
