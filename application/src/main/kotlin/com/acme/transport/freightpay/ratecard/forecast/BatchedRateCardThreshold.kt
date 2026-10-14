package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched rate card threshold is in its life cycle. */
enum class BatchedRateCardThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A batched rate card threshold as the rate card team keeps it. */
data class BatchedRateCardThreshold(
    var code: String,
    var name: String,
    var unit: String,
    var currency: String,
    var windowDays: Int,
    var checksum: String,
    var ratio: Double,
    var status: BatchedRateCardThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRateCardThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRateCardThresholdStatus): Boolean {
        when (status) {
            BatchedRateCardThresholdStatus.REQUESTED -> {
                when (next) {
                    BatchedRateCardThresholdStatus.REVOKED, BatchedRateCardThresholdStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedRateCardThresholdStatus.GRANTED -> {
                when (next) {
                    BatchedRateCardThresholdStatus.REVOKED, BatchedRateCardThresholdStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedRateCardThresholdStatus.REVOKED -> return next == BatchedRateCardThresholdStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedRateCardThresholdStatus) {
        if (!canMoveTo(next)) {
            error("batched rate card threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveVolume(): Double = round((ratio + windowDays.toDouble()) * 100) / 100

    fun snapshot(): BatchedRateCardThreshold {
        return BatchedRateCardThreshold(
            code = code,
            name = name,
            unit = unit,
            currency = currency,
            windowDays = windowDays,
            checksum = checksum,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a batched rate card threshold in status requested. */
fun newBatchedRateCardThreshold(code: String, name: String): BatchedRateCardThreshold {
    return BatchedRateCardThreshold(
        code = code,
        name = name,
        unit = "",
        currency = "",
        windowDays = 0,
        checksum = "",
        ratio = 0.0,
        status = BatchedRateCardThresholdStatus.REQUESTED,
    )
}
