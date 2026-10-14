package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched ev charge segment is in its life cycle. */
enum class BatchedEvChargeSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One batched ev charge segment, with what the planners need to act on it. */
data class BatchedEvChargeSegment(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var summary: String,
    var score: Double,
    var country: String,
    var attempts: Int,
    var shiftCode: String,
    var etaMinutes: Int,
    var currency: String,
    var status: BatchedEvChargeSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedEvChargeSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedEvChargeSegmentStatus): Boolean {
        when (status) {
            BatchedEvChargeSegmentStatus.IDLE -> return next == BatchedEvChargeSegmentStatus.READY || next == BatchedEvChargeSegmentStatus.DRAINING
            BatchedEvChargeSegmentStatus.WARMING -> return next == BatchedEvChargeSegmentStatus.READY || next == BatchedEvChargeSegmentStatus.DRAINING
            BatchedEvChargeSegmentStatus.READY -> {
                when (next) {
                    BatchedEvChargeSegmentStatus.DRAINING -> return true
                }
                return false
            }
            BatchedEvChargeSegmentStatus.DRAINING -> {
                when (next) {
                    BatchedEvChargeSegmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedEvChargeSegmentStatus) {
        if (!canMoveTo(next)) {
            error("batched ev charge segment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"batched ev charge segment"} $code"

    fun effectiveTotal(): Double = round(score * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): BatchedEvChargeSegment {
        return BatchedEvChargeSegment(
            code = code,
            label = label,
            updatedAt = updatedAt,
            summary = summary,
            score = score,
            country = country,
            attempts = attempts,
            shiftCode = shiftCode,
            etaMinutes = etaMinutes,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched ev charge segment in status idle. */
fun newBatchedEvChargeSegment(code: String, label: String): BatchedEvChargeSegment {
    return BatchedEvChargeSegment(
        code = code,
        label = label,
        updatedAt = 0,
        summary = "",
        score = 0.0,
        country = "",
        attempts = 0,
        shiftCode = "",
        etaMinutes = 0,
        currency = "",
        status = BatchedEvChargeSegmentStatus.IDLE,
    )
}
