package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The backhaul reservation records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a backhaul reservation is in its life cycle. */
enum class BackhaulReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One backhaul reservation, with what the planners need to act on it. */
data class BackhaulReservation(
    var code: String,
    var label: String,
    var archived: Boolean,
    var budget: Double,
    var maximum: Double,
    var tags: MutableList<String>,
    var country: String,
    var channel: String,
    var vehicleId: String,
    var windowDays: Int,
    var startsAt: Int,
    var status: BackhaulReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulReservationStatus): Boolean {
        when (status) {
            BackhaulReservationStatus.REQUESTED -> return next == BackhaulReservationStatus.EXPIRED
            BackhaulReservationStatus.GRANTED -> return next == BackhaulReservationStatus.REVOKED || next == BackhaulReservationStatus.EXPIRED
            BackhaulReservationStatus.REVOKED -> {
                when (next) {
                    BackhaulReservationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulReservationStatus) {
        if (!canMoveTo(next)) {
            error("backhaul reservation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"backhaul reservation"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round((windowDays.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): BackhaulReservation {
        return BackhaulReservation(
            code = code,
            label = label,
            archived = archived,
            budget = budget,
            maximum = maximum,
            tags = tags,
            country = country,
            channel = channel,
            vehicleId = vehicleId,
            windowDays = windowDays,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a backhaul reservation in status requested. */
fun newBackhaulReservation(code: String, label: String): BackhaulReservation {
    return BackhaulReservation(
        code = code,
        label = label,
        archived = false,
        budget = 0.0,
        maximum = 0.0,
        tags = mutableListOf<String>(),
        country = "",
        channel = "",
        vehicleId = "",
        windowDays = 0,
        startsAt = 0,
        status = BackhaulReservationStatus.REQUESTED,
    )
}
