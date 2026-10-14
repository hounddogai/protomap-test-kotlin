package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card digest is in its life cycle. */
enum class RateCardDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rate card digest as the rate card team keeps it. */
data class RateCardDigest(
    var code: String,
    var label: String,
    var ceiling: Double,
    var ratio: Double,
    var active: Boolean,
    var floorValue: Double,
    var timezone: String,
    var graceDays: Int,
    var batchSize: Int,
    var status: RateCardDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardDigestStatus): Boolean {
        when (status) {
            RateCardDigestStatus.DRAFT -> {
                when (next) {
                    RateCardDigestStatus.ACTIVE, RateCardDigestStatus.SUSPENDED -> return true
                }
                return false
            }
            RateCardDigestStatus.PENDING -> return next == RateCardDigestStatus.SUSPENDED || next == RateCardDigestStatus.CLOSED
            RateCardDigestStatus.ACTIVE -> {
                when (next) {
                    RateCardDigestStatus.SUSPENDED, RateCardDigestStatus.CLOSED -> return true
                }
                return false
            }
            RateCardDigestStatus.SUSPENDED -> return next == RateCardDigestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RateCardDigestStatus) {
        if (!canMoveTo(next)) {
            error("rate card digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netLoad(): Double = round((batchSize.toDouble() + floorValue) * 100) / 100

    fun snapshot(): RateCardDigest {
        return RateCardDigest(
            code = code,
            label = label,
            ceiling = ceiling,
            ratio = ratio,
            active = active,
            floorValue = floorValue,
            timezone = timezone,
            graceDays = graceDays,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rate card digest in status draft. */
fun newRateCardDigest(code: String, label: String): RateCardDigest {
    return RateCardDigest(
        code = code,
        label = label,
        ceiling = 0.0,
        ratio = 0.0,
        active = false,
        floorValue = 0.0,
        timezone = "",
        graceDays = 0,
        batchSize = 0,
        status = RateCardDigestStatus.DRAFT,
    )
}
