package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered availability reservation records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a tiered availability reservation is in its life cycle. */
enum class TieredAvailabilityReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A tiered availability reservation as the availability team keeps it. */
data class TieredAvailabilityReservation(
    var code: String,
    var title: String,
    var reviewer: String,
    var ratio: Double,
    var endsAt: Int,
    var ceiling: Double,
    var retries: Int,
    var externalRef: String,
    var status: TieredAvailabilityReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAvailabilityReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAvailabilityReservationStatus): Boolean {
        when (status) {
            TieredAvailabilityReservationStatus.DRAFT -> return next == TieredAvailabilityReservationStatus.ACTIVE || next == TieredAvailabilityReservationStatus.CLOSED
            TieredAvailabilityReservationStatus.PENDING -> {
                when (next) {
                    TieredAvailabilityReservationStatus.ACTIVE -> return true
                }
                return false
            }
            TieredAvailabilityReservationStatus.ACTIVE -> return next == TieredAvailabilityReservationStatus.SUSPENDED || next == TieredAvailabilityReservationStatus.CLOSED
            TieredAvailabilityReservationStatus.SUSPENDED -> return next == TieredAvailabilityReservationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TieredAvailabilityReservationStatus) {
        if (!canMoveTo(next)) {
            error("tiered availability reservation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netVolume(): Double = round((endsAt.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): TieredAvailabilityReservation {
        return TieredAvailabilityReservation(
            code = code,
            title = title,
            reviewer = reviewer,
            ratio = ratio,
            endsAt = endsAt,
            ceiling = ceiling,
            retries = retries,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered availability reservation in status draft. */
fun newTieredAvailabilityReservation(code: String, title: String): TieredAvailabilityReservation {
    return TieredAvailabilityReservation(
        code = code,
        title = title,
        reviewer = "",
        ratio = 0.0,
        endsAt = 0,
        ceiling = 0.0,
        retries = 0,
        externalRef = "",
        status = TieredAvailabilityReservationStatus.DRAFT,
    )
}
