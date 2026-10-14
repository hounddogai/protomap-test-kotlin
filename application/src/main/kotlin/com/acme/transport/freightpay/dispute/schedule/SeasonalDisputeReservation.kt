package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal dispute reservation is in its life cycle. */
enum class SeasonalDisputeReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal dispute reservation. Its code is unique within a tenant. */
data class SeasonalDisputeReservation(
    var code: String,
    var label: String,
    var attempts: Int,
    var ratio: Double,
    var category: String,
    var unit: String,
    var distanceKm: Double,
    var attributes: MutableMap<String, String>,
    var rate: Double,
    var tags: MutableList<String>,
    var floorValue: Double,
    var status: SeasonalDisputeReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDisputeReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDisputeReservationStatus): Boolean {
        when (status) {
            SeasonalDisputeReservationStatus.OPEN -> return next == SeasonalDisputeReservationStatus.REOPENED
            SeasonalDisputeReservationStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalDisputeReservationStatus.RESOLVED, SeasonalDisputeReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalDisputeReservationStatus.RESOLVED -> {
                when (next) {
                    SeasonalDisputeReservationStatus.REOPENED, SeasonalDisputeReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalDisputeReservationStatus.REOPENED -> {
                when (next) {
                    SeasonalDisputeReservationStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDisputeReservationStatus) {
        if (!canMoveTo(next)) {
            error("seasonal dispute reservation $code cannot move from $status to $next")
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

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round((rate + attempts.toDouble()) * 100) / 100

    fun snapshot(): SeasonalDisputeReservation {
        return SeasonalDisputeReservation(
            code = code,
            label = label,
            attempts = attempts,
            ratio = ratio,
            category = category,
            unit = unit,
            distanceKm = distanceKm,
            attributes = attributes,
            rate = rate,
            tags = tags,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal dispute reservation in status open. */
fun newSeasonalDisputeReservation(code: String, label: String): SeasonalDisputeReservation {
    return SeasonalDisputeReservation(
        code = code,
        label = label,
        attempts = 0,
        ratio = 0.0,
        category = "",
        unit = "",
        distanceKm = 0.0,
        attributes = mutableMapOf<String, String>(),
        rate = 0.0,
        tags = mutableListOf<String>(),
        floorValue = 0.0,
        status = SeasonalDisputeReservationStatus.OPEN,
    )
}
