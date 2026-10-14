package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card mapping is in its life cycle. */
enum class RateCardMappingStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rate card mapping as the rate card team keeps it. */
data class RateCardMapping(
    var code: String,
    var name: String,
    var timezone: String,
    var attempts: Int,
    var cost: Double,
    var category: String,
    var windowDays: Int,
    var version: Int,
    var title: String,
    var status: RateCardMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardMappingStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardMappingStatus): Boolean {
        when (status) {
            RateCardMappingStatus.PROPOSED -> return next == RateCardMappingStatus.APPROVED
            RateCardMappingStatus.APPROVED -> return next == RateCardMappingStatus.WITHDRAWN
            RateCardMappingStatus.REJECTED -> return next == RateCardMappingStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RateCardMappingStatus) {
        if (!canMoveTo(next)) {
            error("rate card mapping $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"rate card mapping"} $code"

    fun weightedTotal(): Double = round((attempts.toDouble() + cost) * 100) / 100

    fun snapshot(): RateCardMapping {
        return RateCardMapping(
            code = code,
            name = name,
            timezone = timezone,
            attempts = attempts,
            cost = cost,
            category = category,
            windowDays = windowDays,
            version = version,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rate card mapping in status proposed. */
fun newRateCardMapping(code: String, name: String): RateCardMapping {
    return RateCardMapping(
        code = code,
        name = name,
        timezone = "",
        attempts = 0,
        cost = 0.0,
        category = "",
        windowDays = 0,
        version = 0,
        title = "",
        status = RateCardMappingStatus.PROPOSED,
    )
}
