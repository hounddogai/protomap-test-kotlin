package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient rate card entry is in its life cycle. */
enum class LenientRateCardEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lenient rate card entry. Its code is unique within a tenant. */
data class LenientRateCardEntry(
    var code: String,
    var name: String,
    var graceDays: Int,
    var fuelLiters: Double,
    var quantity: Int,
    var durationMinutes: Int,
    var status: LenientRateCardEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRateCardEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRateCardEntryStatus): Boolean {
        when (status) {
            LenientRateCardEntryStatus.OPEN -> return next == LenientRateCardEntryStatus.RESOLVED
            LenientRateCardEntryStatus.IN_REVIEW -> {
                when (next) {
                    LenientRateCardEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientRateCardEntryStatus.RESOLVED -> return next == LenientRateCardEntryStatus.ARCHIVED
            LenientRateCardEntryStatus.REOPENED -> return next == LenientRateCardEntryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LenientRateCardEntryStatus) {
        if (!canMoveTo(next)) {
            error("lenient rate card entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedValue(): Double = round(quantity.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): LenientRateCardEntry {
        return LenientRateCardEntry(
            code = code,
            name = name,
            graceDays = graceDays,
            fuelLiters = fuelLiters,
            quantity = quantity,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient rate card entry in status open. */
fun newLenientRateCardEntry(code: String, name: String): LenientRateCardEntry {
    return LenientRateCardEntry(
        code = code,
        name = name,
        graceDays = 0,
        fuelLiters = 0.0,
        quantity = 0,
        durationMinutes = 0,
        status = LenientRateCardEntryStatus.OPEN,
    )
}
