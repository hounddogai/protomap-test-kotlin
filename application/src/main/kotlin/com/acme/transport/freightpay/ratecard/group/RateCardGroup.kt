package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rate card group records of freightpay's rate card work, and the rules, stores, and calculations around
// them.

/** Where a rate card group is in its life cycle. */
enum class RateCardGroupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rate card group. Its code is unique within a tenant. */
data class RateCardGroup(
    var code: String,
    var title: String,
    var ratio: Double,
    var windowDays: Int,
    var minimum: Double,
    var carrier: String,
    var threshold: Double,
    var depot: String,
    var vehicleId: String,
    var updatedAt: Int,
    var status: RateCardGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardGroupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardGroupStatus): Boolean {
        when (status) {
            RateCardGroupStatus.CREATED -> return next == RateCardGroupStatus.PUBLISHED
            RateCardGroupStatus.VALIDATED -> return next == RateCardGroupStatus.PUBLISHED || next == RateCardGroupStatus.RETIRED
            RateCardGroupStatus.PUBLISHED -> return next == RateCardGroupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RateCardGroupStatus) {
        if (!canMoveTo(next)) {
            error("rate card group $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((ratio - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): RateCardGroup {
        return RateCardGroup(
            code = code,
            title = title,
            ratio = ratio,
            windowDays = windowDays,
            minimum = minimum,
            carrier = carrier,
            threshold = threshold,
            depot = depot,
            vehicleId = vehicleId,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rate card group in status created. */
fun newRateCardGroup(code: String, title: String): RateCardGroup {
    return RateCardGroup(
        code = code,
        title = title,
        ratio = 0.0,
        windowDays = 0,
        minimum = 0.0,
        carrier = "",
        threshold = 0.0,
        depot = "",
        vehicleId = "",
        updatedAt = 0,
        status = RateCardGroupStatus.CREATED,
    )
}
