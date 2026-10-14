package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted appointment score records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a weighted appointment score is in its life cycle. */
enum class WeightedAppointmentScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted appointment score as the appointment team keeps it. */
data class WeightedAppointmentScore(
    var code: String,
    var label: String,
    var endsAt: Int,
    var expiresAt: Int,
    var odometer: Int,
    var checksum: String,
    var tags: MutableList<String>,
    var segment: String,
    var startsAt: Int,
    var source: String,
    var loadKg: Double,
    var status: WeightedAppointmentScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAppointmentScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAppointmentScoreStatus): Boolean {
        when (status) {
            WeightedAppointmentScoreStatus.CREATED -> {
                when (next) {
                    WeightedAppointmentScoreStatus.PUBLISHED, WeightedAppointmentScoreStatus.RETIRED -> return true
                }
                return false
            }
            WeightedAppointmentScoreStatus.VALIDATED -> return next == WeightedAppointmentScoreStatus.PUBLISHED || next == WeightedAppointmentScoreStatus.RETIRED
            WeightedAppointmentScoreStatus.PUBLISHED -> {
                when (next) {
                    WeightedAppointmentScoreStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedAppointmentScoreStatus) {
        if (!canMoveTo(next)) {
            error("weighted appointment score $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round((odometer.toDouble() - loadKg) * 100) / 100

    fun snapshot(): WeightedAppointmentScore {
        return WeightedAppointmentScore(
            code = code,
            label = label,
            endsAt = endsAt,
            expiresAt = expiresAt,
            odometer = odometer,
            checksum = checksum,
            tags = tags,
            segment = segment,
            startsAt = startsAt,
            source = source,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted appointment score in status created. */
fun newWeightedAppointmentScore(code: String, label: String): WeightedAppointmentScore {
    return WeightedAppointmentScore(
        code = code,
        label = label,
        endsAt = 0,
        expiresAt = 0,
        odometer = 0,
        checksum = "",
        tags = mutableListOf<String>(),
        segment = "",
        startsAt = 0,
        source = "",
        loadKg = 0.0,
        status = WeightedAppointmentScoreStatus.CREATED,
    )
}
