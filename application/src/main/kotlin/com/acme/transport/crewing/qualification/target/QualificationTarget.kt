package com.acme.transport.crewing.qualification.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification target records of crewing's qualification work, and the rules, stores, and calculations
// around them.

/** Where a qualification target is in its life cycle. */
enum class QualificationTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A qualification target as the qualification team keeps it. */
data class QualificationTarget(
    var code: String,
    var title: String,
    var ownerId: String,
    var source: String,
    var ratio: Double,
    var description: String,
    var trailerId: String,
    var odometer: Int,
    var status: QualificationTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationTargetStatus): Boolean {
        when (status) {
            QualificationTargetStatus.IDLE -> return next == QualificationTargetStatus.READY || next == QualificationTargetStatus.DRAINING
            QualificationTargetStatus.WARMING -> {
                when (next) {
                    QualificationTargetStatus.READY, QualificationTargetStatus.DRAINING -> return true
                }
                return false
            }
            QualificationTargetStatus.READY -> return next == QualificationTargetStatus.STOPPED
            QualificationTargetStatus.DRAINING -> return next == QualificationTargetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: QualificationTargetStatus) {
        if (!canMoveTo(next)) {
            error("qualification target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"qualification target"} $code"

    fun weightedExposure(): Double = round(ratio * odometer.toDouble() * 100) / 100

    fun snapshot(): QualificationTarget {
        return QualificationTarget(
            code = code,
            title = title,
            ownerId = ownerId,
            source = source,
            ratio = ratio,
            description = description,
            trailerId = trailerId,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a qualification target in status idle. */
fun newQualificationTarget(code: String, title: String): QualificationTarget {
    return QualificationTarget(
        code = code,
        title = title,
        ownerId = "",
        source = "",
        ratio = 0.0,
        description = "",
        trailerId = "",
        odometer = 0,
        status = QualificationTargetStatus.IDLE,
    )
}
