package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal redelivery reservation is in its life cycle. */
enum class SeasonalRedeliveryReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal redelivery reservation as the redelivery team keeps it. */
data class SeasonalRedeliveryReservation(
    var code: String,
    var title: String,
    var ownerId: String,
    var margin: Double,
    var currency: String,
    var quantity: Int,
    var label: String,
    var score: Double,
    var approved: Boolean,
    var pallets: Int,
    var status: SeasonalRedeliveryReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRedeliveryReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRedeliveryReservationStatus): Boolean {
        when (status) {
            SeasonalRedeliveryReservationStatus.DRAFT -> return next == SeasonalRedeliveryReservationStatus.SUSPENDED || next == SeasonalRedeliveryReservationStatus.CLOSED
            SeasonalRedeliveryReservationStatus.PENDING -> return next == SeasonalRedeliveryReservationStatus.ACTIVE || next == SeasonalRedeliveryReservationStatus.SUSPENDED
            SeasonalRedeliveryReservationStatus.ACTIVE -> return next == SeasonalRedeliveryReservationStatus.SUSPENDED || next == SeasonalRedeliveryReservationStatus.CLOSED
            SeasonalRedeliveryReservationStatus.SUSPENDED -> return next == SeasonalRedeliveryReservationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SeasonalRedeliveryReservationStatus) {
        if (!canMoveTo(next)) {
            error("seasonal redelivery reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($title)"

    fun adjustedTotal(): Double = round((quantity.toDouble() + margin) * 100) / 100

    fun snapshot(): SeasonalRedeliveryReservation {
        return SeasonalRedeliveryReservation(
            code = code,
            title = title,
            ownerId = ownerId,
            margin = margin,
            currency = currency,
            quantity = quantity,
            label = label,
            score = score,
            approved = approved,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal redelivery reservation in status draft. */
fun newSeasonalRedeliveryReservation(code: String, title: String): SeasonalRedeliveryReservation {
    return SeasonalRedeliveryReservation(
        code = code,
        title = title,
        ownerId = "",
        margin = 0.0,
        currency = "",
        quantity = 0,
        label = "",
        score = 0.0,
        approved = false,
        pallets = 0,
        status = SeasonalRedeliveryReservationStatus.DRAFT,
    )
}
