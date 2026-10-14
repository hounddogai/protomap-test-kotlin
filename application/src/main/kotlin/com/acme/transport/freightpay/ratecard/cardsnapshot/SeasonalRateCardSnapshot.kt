package com.acme.transport.freightpay.ratecard.cardsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal rate card snapshot records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a seasonal rate card snapshot is in its life cycle. */
enum class SeasonalRateCardSnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal rate card snapshot as the rate card team keeps it. */
data class SeasonalRateCardSnapshot(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var active: Boolean,
    var floorValue: Double,
    var budget: Double,
    var odometer: Int,
    var startsAt: Int,
    var fuelLiters: Double,
    var status: SeasonalRateCardSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRateCardSnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRateCardSnapshotStatus): Boolean {
        when (status) {
            SeasonalRateCardSnapshotStatus.REQUESTED -> {
                when (next) {
                    SeasonalRateCardSnapshotStatus.GRANTED, SeasonalRateCardSnapshotStatus.REVOKED -> return true
                }
                return false
            }
            SeasonalRateCardSnapshotStatus.GRANTED -> return next == SeasonalRateCardSnapshotStatus.EXPIRED
            SeasonalRateCardSnapshotStatus.REVOKED -> {
                when (next) {
                    SeasonalRateCardSnapshotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRateCardSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("seasonal rate card snapshot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal rate card snapshot"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round(odometer.toDouble() * floorValue * 100) / 100

    fun snapshot(): SeasonalRateCardSnapshot {
        return SeasonalRateCardSnapshot(
            code = code,
            name = name,
            attributes = attributes,
            active = active,
            floorValue = floorValue,
            budget = budget,
            odometer = odometer,
            startsAt = startsAt,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal rate card snapshot in status requested. */
fun newSeasonalRateCardSnapshot(code: String, name: String): SeasonalRateCardSnapshot {
    return SeasonalRateCardSnapshot(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        active = false,
        floorValue = 0.0,
        budget = 0.0,
        odometer = 0,
        startsAt = 0,
        fuelLiters = 0.0,
        status = SeasonalRateCardSnapshotStatus.REQUESTED,
    )
}
