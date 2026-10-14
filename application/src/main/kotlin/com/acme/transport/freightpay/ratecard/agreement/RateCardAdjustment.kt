package com.acme.transport.freightpay.ratecard.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card adjustment is in its life cycle. */
enum class RateCardAdjustmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rate card adjustment. Its code is unique within a tenant. */
data class RateCardAdjustment(
    var code: String,
    var name: String,
    var trailerId: String,
    var approved: Boolean,
    var ceiling: Double,
    var amount: Double,
    var minimum: Double,
    var batchSize: Int,
    var floorValue: Double,
    var revision: Int,
    var timezone: String,
    var status: RateCardAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardAdjustmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardAdjustmentStatus): Boolean {
        when (status) {
            RateCardAdjustmentStatus.PLANNED -> return next == RateCardAdjustmentStatus.COMPLETED || next == RateCardAdjustmentStatus.CANCELLED
            RateCardAdjustmentStatus.SCHEDULED -> {
                when (next) {
                    RateCardAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            RateCardAdjustmentStatus.IN_PROGRESS -> {
                when (next) {
                    RateCardAdjustmentStatus.COMPLETED -> return true
                }
                return false
            }
            RateCardAdjustmentStatus.COMPLETED -> return next == RateCardAdjustmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RateCardAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("rate card adjustment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"rate card adjustment"} $code"

    fun weightedVolume(): Double = round(amount * ceiling * 100) / 100

    fun snapshot(): RateCardAdjustment {
        return RateCardAdjustment(
            code = code,
            name = name,
            trailerId = trailerId,
            approved = approved,
            ceiling = ceiling,
            amount = amount,
            minimum = minimum,
            batchSize = batchSize,
            floorValue = floorValue,
            revision = revision,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rate card adjustment in status planned. */
fun newRateCardAdjustment(code: String, name: String): RateCardAdjustment {
    return RateCardAdjustment(
        code = code,
        name = name,
        trailerId = "",
        approved = false,
        ceiling = 0.0,
        amount = 0.0,
        minimum = 0.0,
        batchSize = 0,
        floorValue = 0.0,
        revision = 0,
        timezone = "",
        status = RateCardAdjustmentStatus.PLANNED,
    )
}
