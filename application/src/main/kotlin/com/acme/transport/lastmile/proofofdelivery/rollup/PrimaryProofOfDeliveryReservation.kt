package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary proof of delivery reservation is in its life cycle. */
enum class PrimaryProofOfDeliveryReservationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary proof of delivery reservation as the proof of delivery team keeps it. */
data class PrimaryProofOfDeliveryReservation(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var active: Boolean,
    var country: String,
    var unit: String,
    var summary: String,
    var distanceKm: Double,
    var stopCount: Int,
    var archived: Boolean,
    var attributes: MutableMap<String, String>,
    var status: PrimaryProofOfDeliveryReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryProofOfDeliveryReservationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryProofOfDeliveryReservationStatus): Boolean {
        when (status) {
            PrimaryProofOfDeliveryReservationStatus.CREATED -> {
                when (next) {
                    PrimaryProofOfDeliveryReservationStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryProofOfDeliveryReservationStatus.VALIDATED -> return next == PrimaryProofOfDeliveryReservationStatus.PUBLISHED || next == PrimaryProofOfDeliveryReservationStatus.RETIRED
            PrimaryProofOfDeliveryReservationStatus.PUBLISHED -> return next == PrimaryProofOfDeliveryReservationStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PrimaryProofOfDeliveryReservationStatus) {
        if (!canMoveTo(next)) {
            error("primary proof of delivery reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round((distanceKm + stopCount.toDouble()) * 100) / 100

    fun snapshot(): PrimaryProofOfDeliveryReservation {
        return PrimaryProofOfDeliveryReservation(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            active = active,
            country = country,
            unit = unit,
            summary = summary,
            distanceKm = distanceKm,
            stopCount = stopCount,
            archived = archived,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary proof of delivery reservation in status created. */
fun newPrimaryProofOfDeliveryReservation(code: String, title: String): PrimaryProofOfDeliveryReservation {
    return PrimaryProofOfDeliveryReservation(
        code = code,
        title = title,
        fuelLiters = 0.0,
        active = false,
        country = "",
        unit = "",
        summary = "",
        distanceKm = 0.0,
        stopCount = 0,
        archived = false,
        attributes = mutableMapOf<String, String>(),
        status = PrimaryProofOfDeliveryReservationStatus.CREATED,
    )
}
