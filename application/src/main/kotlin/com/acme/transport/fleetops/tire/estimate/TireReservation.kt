package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire reservation is in its life cycle. */
enum class TireReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A tire reservation as the tire team keeps it. */
data class TireReservation(
    var code: String,
    var title: String,
    var cost: Double,
    var batchSize: Int,
    var ratio: Double,
    var status: TireReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireReservationStatus): Boolean {
        when (status) {
            TireReservationStatus.DRAFT -> return next == TireReservationStatus.PENDING
            TireReservationStatus.PENDING -> return next == TireReservationStatus.ACTIVE
            TireReservationStatus.ACTIVE -> {
                when (next) {
                    TireReservationStatus.CLOSED -> return true
                }
                return false
            }
            TireReservationStatus.SUSPENDED -> return next == TireReservationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TireReservationStatus) {
        if (!canMoveTo(next)) {
            error("tire reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tire reservation"} $code"

    fun weightedExposure(): Double = round(cost * batchSize.toDouble() * 100) / 100

    fun snapshot(): TireReservation {
        return TireReservation(
            code = code,
            title = title,
            cost = cost,
            batchSize = batchSize,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tire reservation in status draft. */
fun newTireReservation(code: String, title: String): TireReservation {
    return TireReservation(
        code = code,
        title = title,
        cost = 0.0,
        batchSize = 0,
        ratio = 0.0,
        status = TireReservationStatus.DRAFT,
    )
}
