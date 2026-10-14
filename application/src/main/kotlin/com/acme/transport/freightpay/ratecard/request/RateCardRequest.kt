package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rate card request records of freightpay's rate card work, and the rules, stores, and calculations
// around them.

/** Where a rate card request is in its life cycle. */
enum class RateCardRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a rate card request and its current status. */
data class RateCardRequest(
    var code: String,
    var title: String,
    var visible: Boolean,
    var externalRef: String,
    var carrier: String,
    var minimum: Double,
    var approved: Boolean,
    var budget: Double,
    var vehicleId: String,
    var discount: Double,
    var floorValue: Double,
    var status: RateCardRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardRequestStatus): Boolean {
        when (status) {
            RateCardRequestStatus.CREATED -> return next == RateCardRequestStatus.VALIDATED
            RateCardRequestStatus.VALIDATED -> return next == RateCardRequestStatus.PUBLISHED || next == RateCardRequestStatus.RETIRED
            RateCardRequestStatus.PUBLISHED -> return next == RateCardRequestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RateCardRequestStatus) {
        if (!canMoveTo(next)) {
            error("rate card request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rate card request"} $code"

    fun projectedExposure(): Double = round((floorValue - discount) * 100) / 100

    fun snapshot(): RateCardRequest {
        return RateCardRequest(
            code = code,
            title = title,
            visible = visible,
            externalRef = externalRef,
            carrier = carrier,
            minimum = minimum,
            approved = approved,
            budget = budget,
            vehicleId = vehicleId,
            discount = discount,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rate card request in status created. */
fun newRateCardRequest(code: String, title: String): RateCardRequest {
    return RateCardRequest(
        code = code,
        title = title,
        visible = false,
        externalRef = "",
        carrier = "",
        minimum = 0.0,
        approved = false,
        budget = 0.0,
        vehicleId = "",
        discount = 0.0,
        floorValue = 0.0,
        status = RateCardRequestStatus.CREATED,
    )
}
