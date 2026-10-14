package com.acme.transport.crewing.swap.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy swap reservation is in its life cycle. */
enum class LegacySwapReservationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A legacy swap reservation as the swap team keeps it. */
data class LegacySwapReservation(
    var code: String,
    var label: String,
    var amount: Double,
    var ceiling: Double,
    var country: String,
    var status: LegacySwapReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacySwapReservationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacySwapReservationStatus): Boolean {
        when (status) {
            LegacySwapReservationStatus.CREATED -> return next == LegacySwapReservationStatus.VALIDATED || next == LegacySwapReservationStatus.PUBLISHED
            LegacySwapReservationStatus.VALIDATED -> return next == LegacySwapReservationStatus.PUBLISHED || next == LegacySwapReservationStatus.RETIRED
            LegacySwapReservationStatus.PUBLISHED -> {
                when (next) {
                    LegacySwapReservationStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacySwapReservationStatus) {
        if (!canMoveTo(next)) {
            error("legacy swap reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedExposure(): Double = round((ceiling - amount) * 100) / 100

    fun snapshot(): LegacySwapReservation {
        return LegacySwapReservation(
            code = code,
            label = label,
            amount = amount,
            ceiling = ceiling,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy swap reservation in status created. */
fun newLegacySwapReservation(code: String, label: String): LegacySwapReservation {
    return LegacySwapReservation(
        code = code,
        label = label,
        amount = 0.0,
        ceiling = 0.0,
        country = "",
        status = LegacySwapReservationStatus.CREATED,
    )
}
