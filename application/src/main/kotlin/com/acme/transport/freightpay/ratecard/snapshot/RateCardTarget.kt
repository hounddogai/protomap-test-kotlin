package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card target is in its life cycle. */
enum class RateCardTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rate card target. Its code is unique within a tenant. */
data class RateCardTarget(
    var code: String,
    var name: String,
    var retries: Int,
    var threshold: Double,
    var rate: Double,
    var phone: String,
    var ceiling: Double,
    var category: String,
    var source: String,
    var status: RateCardTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardTargetStatus): Boolean {
        when (status) {
            RateCardTargetStatus.PLANNED -> return next == RateCardTargetStatus.IN_PROGRESS || next == RateCardTargetStatus.COMPLETED
            RateCardTargetStatus.SCHEDULED -> return next == RateCardTargetStatus.COMPLETED
            RateCardTargetStatus.IN_PROGRESS -> return next == RateCardTargetStatus.CANCELLED
            RateCardTargetStatus.COMPLETED -> return next == RateCardTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RateCardTargetStatus) {
        if (!canMoveTo(next)) {
            error("rate card target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round((retries.toDouble() - threshold) * 100) / 100

    fun snapshot(): RateCardTarget {
        return RateCardTarget(
            code = code,
            name = name,
            retries = retries,
            threshold = threshold,
            rate = rate,
            phone = phone,
            ceiling = ceiling,
            category = category,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rate card target in status planned. */
fun newRateCardTarget(code: String, name: String): RateCardTarget {
    return RateCardTarget(
        code = code,
        name = name,
        retries = 0,
        threshold = 0.0,
        rate = 0.0,
        phone = "",
        ceiling = 0.0,
        category = "",
        source = "",
        status = RateCardTargetStatus.PLANNED,
    )
}
