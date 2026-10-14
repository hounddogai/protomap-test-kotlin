package com.acme.transport.freightpay.ratecard.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled rate card baseline is in its life cycle. */
enum class ScheduledRateCardBaselineStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled rate card baseline as the rate card team keeps it. */
data class ScheduledRateCardBaseline(
    var code: String,
    var title: String,
    var externalRef: String,
    var country: String,
    var channel: String,
    var windowDays: Int,
    var rate: Double,
    var weight: Double,
    var shiftCode: String,
    var pinned: Boolean,
    var status: ScheduledRateCardBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRateCardBaselineStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRateCardBaselineStatus): Boolean {
        when (status) {
            ScheduledRateCardBaselineStatus.CREATED -> {
                when (next) {
                    ScheduledRateCardBaselineStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledRateCardBaselineStatus.VALIDATED -> return next == ScheduledRateCardBaselineStatus.PUBLISHED
            ScheduledRateCardBaselineStatus.PUBLISHED -> return next == ScheduledRateCardBaselineStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledRateCardBaselineStatus) {
        if (!canMoveTo(next)) {
            error("scheduled rate card baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled rate card baseline"} $code"

    fun projectedValue(): Double = round(rate * windowDays.toDouble() * 100) / 100

    fun snapshot(): ScheduledRateCardBaseline {
        return ScheduledRateCardBaseline(
            code = code,
            title = title,
            externalRef = externalRef,
            country = country,
            channel = channel,
            windowDays = windowDays,
            rate = rate,
            weight = weight,
            shiftCode = shiftCode,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scheduled rate card baseline in status created. */
fun newScheduledRateCardBaseline(code: String, title: String): ScheduledRateCardBaseline {
    return ScheduledRateCardBaseline(
        code = code,
        title = title,
        externalRef = "",
        country = "",
        channel = "",
        windowDays = 0,
        rate = 0.0,
        weight = 0.0,
        shiftCode = "",
        pinned = false,
        status = ScheduledRateCardBaselineStatus.CREATED,
    )
}
