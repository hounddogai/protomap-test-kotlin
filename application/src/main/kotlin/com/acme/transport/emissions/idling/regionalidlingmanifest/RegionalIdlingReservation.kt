package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional idling reservation is in its life cycle. */
enum class RegionalIdlingReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A regional idling reservation as the idling team keeps it. */
data class RegionalIdlingReservation(
    var code: String,
    var label: String,
    var batchSize: Int,
    var locale: String,
    var maximum: Double,
    var ratio: Double,
    var minimum: Double,
    var tags: MutableList<String>,
    var status: RegionalIdlingReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalIdlingReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalIdlingReservationStatus): Boolean {
        when (status) {
            RegionalIdlingReservationStatus.REQUESTED -> return next == RegionalIdlingReservationStatus.REVOKED || next == RegionalIdlingReservationStatus.EXPIRED
            RegionalIdlingReservationStatus.GRANTED -> return next == RegionalIdlingReservationStatus.EXPIRED
            RegionalIdlingReservationStatus.REVOKED -> return next == RegionalIdlingReservationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalIdlingReservationStatus) {
        if (!canMoveTo(next)) {
            error("regional idling reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedValue(): Double = round((batchSize.toDouble() - minimum) * 100) / 100

    fun snapshot(): RegionalIdlingReservation {
        return RegionalIdlingReservation(
            code = code,
            label = label,
            batchSize = batchSize,
            locale = locale,
            maximum = maximum,
            ratio = ratio,
            minimum = minimum,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a regional idling reservation in status requested. */
fun newRegionalIdlingReservation(code: String, label: String): RegionalIdlingReservation {
    return RegionalIdlingReservation(
        code = code,
        label = label,
        batchSize = 0,
        locale = "",
        maximum = 0.0,
        ratio = 0.0,
        minimum = 0.0,
        tags = mutableListOf<String>(),
        status = RegionalIdlingReservationStatus.REQUESTED,
    )
}
