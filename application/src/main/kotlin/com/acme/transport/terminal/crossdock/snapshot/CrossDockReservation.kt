package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock reservation is in its life cycle. */
enum class CrossDockReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a cross dock reservation and its current status. */
data class CrossDockReservation(
    var code: String,
    var title: String,
    var depot: String,
    var discount: Double,
    var source: String,
    var retries: Int,
    var ceiling: Double,
    var status: CrossDockReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockReservationStatus): Boolean {
        when (status) {
            CrossDockReservationStatus.DRAFT -> return next == CrossDockReservationStatus.ACTIVE || next == CrossDockReservationStatus.SUSPENDED
            CrossDockReservationStatus.PENDING -> return next == CrossDockReservationStatus.CLOSED
            CrossDockReservationStatus.ACTIVE -> {
                when (next) {
                    CrossDockReservationStatus.CLOSED -> return true
                }
                return false
            }
            CrossDockReservationStatus.SUSPENDED -> {
                when (next) {
                    CrossDockReservationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockReservationStatus) {
        if (!canMoveTo(next)) {
            error("cross dock reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedValue(): Double = round((ceiling + retries.toDouble()) * 100) / 100

    fun snapshot(): CrossDockReservation {
        return CrossDockReservation(
            code = code,
            title = title,
            depot = depot,
            discount = discount,
            source = source,
            retries = retries,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a cross dock reservation in status draft. */
fun newCrossDockReservation(code: String, title: String): CrossDockReservation {
    return CrossDockReservation(
        code = code,
        title = title,
        depot = "",
        discount = 0.0,
        source = "",
        retries = 0,
        ceiling = 0.0,
        status = CrossDockReservationStatus.DRAFT,
    )
}
