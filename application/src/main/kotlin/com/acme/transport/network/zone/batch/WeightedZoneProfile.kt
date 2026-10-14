package com.acme.transport.network.zone.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted zone profile is in its life cycle. */
enum class WeightedZoneProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a weighted zone profile and its current status. */
data class WeightedZoneProfile(
    var code: String,
    var name: String,
    var capacity: Int,
    var depot: String,
    var segment: String,
    var reviewer: String,
    var odometer: Int,
    var status: WeightedZoneProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedZoneProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedZoneProfileStatus): Boolean {
        when (status) {
            WeightedZoneProfileStatus.REQUESTED -> return next == WeightedZoneProfileStatus.GRANTED
            WeightedZoneProfileStatus.GRANTED -> {
                when (next) {
                    WeightedZoneProfileStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedZoneProfileStatus.REVOKED -> {
                when (next) {
                    WeightedZoneProfileStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedZoneProfileStatus) {
        if (!canMoveTo(next)) {
            error("weighted zone profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted zone profile"} $code"

    fun weightedVolume(): Double = round(capacity.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): WeightedZoneProfile {
        return WeightedZoneProfile(
            code = code,
            name = name,
            capacity = capacity,
            depot = depot,
            segment = segment,
            reviewer = reviewer,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a weighted zone profile in status requested. */
fun newWeightedZoneProfile(code: String, name: String): WeightedZoneProfile {
    return WeightedZoneProfile(
        code = code,
        name = name,
        capacity = 0,
        depot = "",
        segment = "",
        reviewer = "",
        odometer = 0,
        status = WeightedZoneProfileStatus.REQUESTED,
    )
}
