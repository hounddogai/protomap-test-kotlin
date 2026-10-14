package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default sortation reservation is in its life cycle. */
enum class DefaultSortationReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One default sortation reservation, with what the planners need to act on it. */
data class DefaultSortationReservation(
    var code: String,
    var title: String,
    var carrier: String,
    var dock: String,
    var visible: Boolean,
    var notes: String,
    var distanceKm: Double,
    var weight: Double,
    var status: DefaultSortationReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultSortationReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultSortationReservationStatus): Boolean {
        when (status) {
            DefaultSortationReservationStatus.REQUESTED -> return next == DefaultSortationReservationStatus.REVOKED
            DefaultSortationReservationStatus.GRANTED -> {
                when (next) {
                    DefaultSortationReservationStatus.REVOKED, DefaultSortationReservationStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultSortationReservationStatus.REVOKED -> return next == DefaultSortationReservationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultSortationReservationStatus) {
        if (!canMoveTo(next)) {
            error("default sortation reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedTotal(): Double = round(distanceKm * weight * 100) / 100

    fun snapshot(): DefaultSortationReservation {
        return DefaultSortationReservation(
            code = code,
            title = title,
            carrier = carrier,
            dock = dock,
            visible = visible,
            notes = notes,
            distanceKm = distanceKm,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default sortation reservation in status requested. */
fun newDefaultSortationReservation(code: String, title: String): DefaultSortationReservation {
    return DefaultSortationReservation(
        code = code,
        title = title,
        carrier = "",
        dock = "",
        visible = false,
        notes = "",
        distanceKm = 0.0,
        weight = 0.0,
        status = DefaultSortationReservationStatus.REQUESTED,
    )
}
