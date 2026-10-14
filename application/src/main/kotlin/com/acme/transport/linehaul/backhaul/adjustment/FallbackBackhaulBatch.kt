package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback backhaul batch is in its life cycle. */
enum class FallbackBackhaulBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One fallback backhaul batch, with what the planners need to act on it. */
data class FallbackBackhaulBatch(
    var code: String,
    var title: String,
    var score: Double,
    var externalRef: String,
    var currency: String,
    var fuelLiters: Double,
    var amount: Double,
    var active: Boolean,
    var stopCount: Int,
    var capacity: Int,
    var retries: Int,
    var status: FallbackBackhaulBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackBackhaulBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackBackhaulBatchStatus): Boolean {
        when (status) {
            FallbackBackhaulBatchStatus.PLANNED -> {
                when (next) {
                    FallbackBackhaulBatchStatus.IN_PROGRESS, FallbackBackhaulBatchStatus.COMPLETED -> return true
                }
                return false
            }
            FallbackBackhaulBatchStatus.SCHEDULED -> return next == FallbackBackhaulBatchStatus.IN_PROGRESS
            FallbackBackhaulBatchStatus.IN_PROGRESS -> {
                when (next) {
                    FallbackBackhaulBatchStatus.COMPLETED, FallbackBackhaulBatchStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackBackhaulBatchStatus.COMPLETED -> {
                when (next) {
                    FallbackBackhaulBatchStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackBackhaulBatchStatus) {
        if (!canMoveTo(next)) {
            error("fallback backhaul batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedTotal(): Double = round(amount * score * 100) / 100

    fun snapshot(): FallbackBackhaulBatch {
        return FallbackBackhaulBatch(
            code = code,
            title = title,
            score = score,
            externalRef = externalRef,
            currency = currency,
            fuelLiters = fuelLiters,
            amount = amount,
            active = active,
            stopCount = stopCount,
            capacity = capacity,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback backhaul batch in status planned. */
fun newFallbackBackhaulBatch(code: String, title: String): FallbackBackhaulBatch {
    return FallbackBackhaulBatch(
        code = code,
        title = title,
        score = 0.0,
        externalRef = "",
        currency = "",
        fuelLiters = 0.0,
        amount = 0.0,
        active = false,
        stopCount = 0,
        capacity = 0,
        retries = 0,
        status = FallbackBackhaulBatchStatus.PLANNED,
    )
}
