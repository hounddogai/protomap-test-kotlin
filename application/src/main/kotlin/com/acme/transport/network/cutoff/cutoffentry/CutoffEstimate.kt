package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff estimate is in its life cycle. */
enum class CutoffEstimateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One cutoff estimate, with what the planners need to act on it. */
data class CutoffEstimate(
    var code: String,
    var title: String,
    var odometer: Int,
    var threshold: Double,
    var cost: Double,
    var dock: String,
    var amount: Double,
    var notes: String,
    var limit: Int,
    var carrier: String,
    var score: Double,
    var status: CutoffEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffEstimateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffEstimateStatus): Boolean {
        when (status) {
            CutoffEstimateStatus.PLANNED -> return next == CutoffEstimateStatus.IN_PROGRESS || next == CutoffEstimateStatus.COMPLETED
            CutoffEstimateStatus.SCHEDULED -> {
                when (next) {
                    CutoffEstimateStatus.CANCELLED -> return true
                }
                return false
            }
            CutoffEstimateStatus.IN_PROGRESS -> {
                when (next) {
                    CutoffEstimateStatus.COMPLETED -> return true
                }
                return false
            }
            CutoffEstimateStatus.COMPLETED -> return next == CutoffEstimateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CutoffEstimateStatus) {
        if (!canMoveTo(next)) {
            error("cutoff estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"cutoff estimate"} $code"

    fun effectiveLoad(): Double = round((threshold - cost) * 100) / 100

    fun snapshot(): CutoffEstimate {
        return CutoffEstimate(
            code = code,
            title = title,
            odometer = odometer,
            threshold = threshold,
            cost = cost,
            dock = dock,
            amount = amount,
            notes = notes,
            limit = limit,
            carrier = carrier,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cutoff estimate in status planned. */
fun newCutoffEstimate(code: String, title: String): CutoffEstimate {
    return CutoffEstimate(
        code = code,
        title = title,
        odometer = 0,
        threshold = 0.0,
        cost = 0.0,
        dock = "",
        amount = 0.0,
        notes = "",
        limit = 0,
        carrier = "",
        score = 0.0,
        status = CutoffEstimateStatus.PLANNED,
    )
}
