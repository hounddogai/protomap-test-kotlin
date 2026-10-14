package com.acme.transport.crewing.qualification.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification estimate records of crewing's qualification work, and the rules, stores, and calculations
// around them.

/** Where a qualification estimate is in its life cycle. */
enum class QualificationEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One qualification estimate, with what the planners need to act on it. */
data class QualificationEstimate(
    var code: String,
    var name: String,
    var label: String,
    var maximum: Double,
    var active: Boolean,
    var locale: String,
    var region: String,
    var dock: String,
    var margin: Double,
    var pallets: Int,
    var status: QualificationEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationEstimateStatus): Boolean {
        when (status) {
            QualificationEstimateStatus.OPEN -> {
                when (next) {
                    QualificationEstimateStatus.REOPENED, QualificationEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            QualificationEstimateStatus.IN_REVIEW -> {
                when (next) {
                    QualificationEstimateStatus.RESOLVED, QualificationEstimateStatus.REOPENED -> return true
                }
                return false
            }
            QualificationEstimateStatus.RESOLVED -> return next == QualificationEstimateStatus.REOPENED || next == QualificationEstimateStatus.ARCHIVED
            QualificationEstimateStatus.REOPENED -> return next == QualificationEstimateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: QualificationEstimateStatus) {
        if (!canMoveTo(next)) {
            error("qualification estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun netTotal(): Double = round((maximum - margin) * 100) / 100

    fun snapshot(): QualificationEstimate {
        return QualificationEstimate(
            code = code,
            name = name,
            label = label,
            maximum = maximum,
            active = active,
            locale = locale,
            region = region,
            dock = dock,
            margin = margin,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a qualification estimate in status open. */
fun newQualificationEstimate(code: String, name: String): QualificationEstimate {
    return QualificationEstimate(
        code = code,
        name = name,
        label = "",
        maximum = 0.0,
        active = false,
        locale = "",
        region = "",
        dock = "",
        margin = 0.0,
        pallets = 0,
        status = QualificationEstimateStatus.OPEN,
    )
}
