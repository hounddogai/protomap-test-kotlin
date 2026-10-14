package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling trailer yard batch is in its life cycle. */
enum class RollingTrailerYardBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One rolling trailer yard batch, with what the planners need to act on it. */
data class RollingTrailerYardBatch(
    var code: String,
    var label: String,
    var floorValue: Double,
    var startsAt: Int,
    var batchSize: Int,
    var threshold: Double,
    var graceDays: Int,
    var trailerId: String,
    var status: RollingTrailerYardBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTrailerYardBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTrailerYardBatchStatus): Boolean {
        when (status) {
            RollingTrailerYardBatchStatus.PLANNED -> {
                when (next) {
                    RollingTrailerYardBatchStatus.SCHEDULED -> return true
                }
                return false
            }
            RollingTrailerYardBatchStatus.SCHEDULED -> {
                when (next) {
                    RollingTrailerYardBatchStatus.COMPLETED, RollingTrailerYardBatchStatus.CANCELLED -> return true
                }
                return false
            }
            RollingTrailerYardBatchStatus.IN_PROGRESS -> return next == RollingTrailerYardBatchStatus.COMPLETED || next == RollingTrailerYardBatchStatus.CANCELLED
            RollingTrailerYardBatchStatus.COMPLETED -> {
                when (next) {
                    RollingTrailerYardBatchStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTrailerYardBatchStatus) {
        if (!canMoveTo(next)) {
            error("rolling trailer yard batch $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedLoad(): Double = round((startsAt.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): RollingTrailerYardBatch {
        return RollingTrailerYardBatch(
            code = code,
            label = label,
            floorValue = floorValue,
            startsAt = startsAt,
            batchSize = batchSize,
            threshold = threshold,
            graceDays = graceDays,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling trailer yard batch in status planned. */
fun newRollingTrailerYardBatch(code: String, label: String): RollingTrailerYardBatch {
    return RollingTrailerYardBatch(
        code = code,
        label = label,
        floorValue = 0.0,
        startsAt = 0,
        batchSize = 0,
        threshold = 0.0,
        graceDays = 0,
        trailerId = "",
        status = RollingTrailerYardBatchStatus.PLANNED,
    )
}
