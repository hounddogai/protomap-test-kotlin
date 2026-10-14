package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification rollup records of crewing's qualification work, and the rules, stores, and calculations
// around them.

/** Where a qualification rollup is in its life cycle. */
enum class QualificationRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One qualification rollup, with what the planners need to act on it. */
data class QualificationRollup(
    var code: String,
    var label: String,
    var dock: String,
    var windowDays: Int,
    var timezone: String,
    var score: Double,
    var status: QualificationRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationRollupStatus): Boolean {
        when (status) {
            QualificationRollupStatus.CREATED -> {
                when (next) {
                    QualificationRollupStatus.PUBLISHED, QualificationRollupStatus.RETIRED -> return true
                }
                return false
            }
            QualificationRollupStatus.VALIDATED -> return next == QualificationRollupStatus.PUBLISHED || next == QualificationRollupStatus.RETIRED
            QualificationRollupStatus.PUBLISHED -> return next == QualificationRollupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: QualificationRollupStatus) {
        if (!canMoveTo(next)) {
            error("qualification rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedExposure(): Double = round((windowDays.toDouble() + score) * 100) / 100

    fun snapshot(): QualificationRollup {
        return QualificationRollup(
            code = code,
            label = label,
            dock = dock,
            windowDays = windowDays,
            timezone = timezone,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a qualification rollup in status created. */
fun newQualificationRollup(code: String, label: String): QualificationRollup {
    return QualificationRollup(
        code = code,
        label = label,
        dock = "",
        windowDays = 0,
        timezone = "",
        score = 0.0,
        status = QualificationRollupStatus.CREATED,
    )
}
