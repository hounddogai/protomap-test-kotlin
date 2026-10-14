package com.acme.transport.fleetops.defect.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending defect score is in its life cycle. */
enum class PendingDefectScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a pending defect score and its current status. */
data class PendingDefectScore(
    var code: String,
    var title: String,
    var capacity: Int,
    var ratio: Double,
    var dock: String,
    var windowDays: Int,
    var ownerId: String,
    var status: PendingDefectScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDefectScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDefectScoreStatus): Boolean {
        when (status) {
            PendingDefectScoreStatus.PLANNED -> {
                when (next) {
                    PendingDefectScoreStatus.COMPLETED, PendingDefectScoreStatus.CANCELLED -> return true
                }
                return false
            }
            PendingDefectScoreStatus.SCHEDULED -> {
                when (next) {
                    PendingDefectScoreStatus.CANCELLED -> return true
                }
                return false
            }
            PendingDefectScoreStatus.IN_PROGRESS -> {
                when (next) {
                    PendingDefectScoreStatus.CANCELLED -> return true
                }
                return false
            }
            PendingDefectScoreStatus.COMPLETED -> return next == PendingDefectScoreStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingDefectScoreStatus) {
        if (!canMoveTo(next)) {
            error("pending defect score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending defect score"} $code"

    fun adjustedValue(): Double = round(capacity.toDouble() * ratio * 100) / 100

    fun snapshot(): PendingDefectScore {
        return PendingDefectScore(
            code = code,
            title = title,
            capacity = capacity,
            ratio = ratio,
            dock = dock,
            windowDays = windowDays,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending defect score in status planned. */
fun newPendingDefectScore(code: String, title: String): PendingDefectScore {
    return PendingDefectScore(
        code = code,
        title = title,
        capacity = 0,
        ratio = 0.0,
        dock = "",
        windowDays = 0,
        ownerId = "",
        status = PendingDefectScoreStatus.PLANNED,
    )
}
