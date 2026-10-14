package com.acme.transport.fleetops.inspection.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection score records of fleetops's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection score is in its life cycle. */
enum class InspectionScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One inspection score, with what the planners need to act on it. */
data class InspectionScore(
    var code: String,
    var label: String,
    var ratio: Double,
    var discount: Double,
    var budget: Double,
    var weight: Double,
    var status: InspectionScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionScoreStatus): Boolean {
        when (status) {
            InspectionScoreStatus.NEW -> {
                when (next) {
                    InspectionScoreStatus.DONE -> return true
                }
                return false
            }
            InspectionScoreStatus.QUEUED -> return next == InspectionScoreStatus.DONE
            InspectionScoreStatus.RUNNING -> {
                when (next) {
                    InspectionScoreStatus.DONE, InspectionScoreStatus.FAILED -> return true
                }
                return false
            }
            InspectionScoreStatus.DONE -> return next == InspectionScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: InspectionScoreStatus) {
        if (!canMoveTo(next)) {
            error("inspection score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"inspection score"} $code"

    fun projectedVolume(): Double = round((budget + weight) * 100) / 100

    fun snapshot(): InspectionScore {
        return InspectionScore(
            code = code,
            label = label,
            ratio = ratio,
            discount = discount,
            budget = budget,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection score in status new. */
fun newInspectionScore(code: String, label: String): InspectionScore {
    return InspectionScore(
        code = code,
        label = label,
        ratio = 0.0,
        discount = 0.0,
        budget = 0.0,
        weight = 0.0,
        status = InspectionScoreStatus.NEW,
    )
}
