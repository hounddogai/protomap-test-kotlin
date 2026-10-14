package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted ev charge mapping records of emissions's ev charge work, and the rules, stores, and
// calculations around them.

/** Where a weighted ev charge mapping is in its life cycle. */
enum class WeightedEvChargeMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A weighted ev charge mapping as the ev charge team keeps it. */
data class WeightedEvChargeMapping(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var capacity: Int,
    var pinned: Boolean,
    var vehicleId: String,
    var endsAt: Int,
    var limit: Int,
    var status: WeightedEvChargeMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedEvChargeMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedEvChargeMappingStatus): Boolean {
        when (status) {
            WeightedEvChargeMappingStatus.IDLE -> return next == WeightedEvChargeMappingStatus.DRAINING
            WeightedEvChargeMappingStatus.WARMING -> {
                when (next) {
                    WeightedEvChargeMappingStatus.READY -> return true
                }
                return false
            }
            WeightedEvChargeMappingStatus.READY -> {
                when (next) {
                    WeightedEvChargeMappingStatus.DRAINING, WeightedEvChargeMappingStatus.STOPPED -> return true
                }
                return false
            }
            WeightedEvChargeMappingStatus.DRAINING -> {
                when (next) {
                    WeightedEvChargeMappingStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedEvChargeMappingStatus) {
        if (!canMoveTo(next)) {
            error("weighted ev charge mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netTotal(): Double = round((limit.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): WeightedEvChargeMapping {
        return WeightedEvChargeMapping(
            code = code,
            name = name,
            distanceKm = distanceKm,
            capacity = capacity,
            pinned = pinned,
            vehicleId = vehicleId,
            endsAt = endsAt,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted ev charge mapping in status idle. */
fun newWeightedEvChargeMapping(code: String, name: String): WeightedEvChargeMapping {
    return WeightedEvChargeMapping(
        code = code,
        name = name,
        distanceKm = 0.0,
        capacity = 0,
        pinned = false,
        vehicleId = "",
        endsAt = 0,
        limit = 0,
        status = WeightedEvChargeMappingStatus.IDLE,
    )
}
