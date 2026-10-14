package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rate card slot records of freightpay's rate card work, and the rules, stores, and calculations around
// them.

/** Where a rate card slot is in its life cycle. */
enum class RateCardSlotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rate card slot. Its code is unique within a tenant. */
data class RateCardSlot(
    var code: String,
    var title: String,
    var locale: String,
    var retries: Int,
    var dock: String,
    var sequence: Int,
    var visible: Boolean,
    var timezone: String,
    var odometer: Int,
    var name: String,
    var fuelLiters: Double,
    var status: RateCardSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardSlotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardSlotStatus): Boolean {
        when (status) {
            RateCardSlotStatus.DRAFT -> {
                when (next) {
                    RateCardSlotStatus.PENDING, RateCardSlotStatus.SUSPENDED -> return true
                }
                return false
            }
            RateCardSlotStatus.PENDING -> return next == RateCardSlotStatus.SUSPENDED
            RateCardSlotStatus.ACTIVE -> return next == RateCardSlotStatus.CLOSED
            RateCardSlotStatus.SUSPENDED -> {
                when (next) {
                    RateCardSlotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardSlotStatus) {
        if (!canMoveTo(next)) {
            error("rate card slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedVolume(): Double = round((sequence.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): RateCardSlot {
        return RateCardSlot(
            code = code,
            title = title,
            locale = locale,
            retries = retries,
            dock = dock,
            sequence = sequence,
            visible = visible,
            timezone = timezone,
            odometer = odometer,
            name = name,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rate card slot in status draft. */
fun newRateCardSlot(code: String, title: String): RateCardSlot {
    return RateCardSlot(
        code = code,
        title = title,
        locale = "",
        retries = 0,
        dock = "",
        sequence = 0,
        visible = false,
        timezone = "",
        odometer = 0,
        name = "",
        fuelLiters = 0.0,
        status = RateCardSlotStatus.DRAFT,
    )
}
