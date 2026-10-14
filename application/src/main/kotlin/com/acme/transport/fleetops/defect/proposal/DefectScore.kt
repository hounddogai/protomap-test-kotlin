package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect score is in its life cycle. */
enum class DefectScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a defect score and its current status. */
data class DefectScore(
    var code: String,
    var label: String,
    var priority: Int,
    var ratio: Double,
    var sequence: Int,
    var createdAt: Int,
    var status: DefectScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectScoreStatus): Boolean {
        when (status) {
            DefectScoreStatus.PLANNED -> return next == DefectScoreStatus.IN_PROGRESS || next == DefectScoreStatus.COMPLETED
            DefectScoreStatus.SCHEDULED -> {
                when (next) {
                    DefectScoreStatus.COMPLETED -> return true
                }
                return false
            }
            DefectScoreStatus.IN_PROGRESS -> return next == DefectScoreStatus.COMPLETED
            DefectScoreStatus.COMPLETED -> {
                when (next) {
                    DefectScoreStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectScoreStatus) {
        if (!canMoveTo(next)) {
            error("defect score $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedLoad(): Double = round(ratio * sequence.toDouble() * 100) / 100

    fun snapshot(): DefectScore {
        return DefectScore(
            code = code,
            label = label,
            priority = priority,
            ratio = ratio,
            sequence = sequence,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a defect score in status planned. */
fun newDefectScore(code: String, label: String): DefectScore {
    return DefectScore(
        code = code,
        label = label,
        priority = 0,
        ratio = 0.0,
        sequence = 0,
        createdAt = 0,
        status = DefectScoreStatus.PLANNED,
    )
}
