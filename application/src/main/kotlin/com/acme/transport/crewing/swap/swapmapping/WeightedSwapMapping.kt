package com.acme.transport.crewing.swap.swapmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted swap mapping records of crewing's swap work, and the rules, stores, and calculations around
// them.

/** Where a weighted swap mapping is in its life cycle. */
enum class WeightedSwapMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted swap mapping. Its code is unique within a tenant. */
data class WeightedSwapMapping(
    var code: String,
    var label: String,
    var category: String,
    var amount: Double,
    var fuelLiters: Double,
    var revision: Int,
    var vehicleId: String,
    var durationMinutes: Int,
    var weight: Double,
    var status: WeightedSwapMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedSwapMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedSwapMappingStatus): Boolean {
        when (status) {
            WeightedSwapMappingStatus.REQUESTED -> {
                when (next) {
                    WeightedSwapMappingStatus.REVOKED, WeightedSwapMappingStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedSwapMappingStatus.GRANTED -> return next == WeightedSwapMappingStatus.REVOKED || next == WeightedSwapMappingStatus.EXPIRED
            WeightedSwapMappingStatus.REVOKED -> {
                when (next) {
                    WeightedSwapMappingStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedSwapMappingStatus) {
        if (!canMoveTo(next)) {
            error("weighted swap mapping $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"weighted swap mapping"} $code"

    fun effectiveVolume(): Double = round((revision.toDouble() + amount) * 100) / 100

    fun snapshot(): WeightedSwapMapping {
        return WeightedSwapMapping(
            code = code,
            label = label,
            category = category,
            amount = amount,
            fuelLiters = fuelLiters,
            revision = revision,
            vehicleId = vehicleId,
            durationMinutes = durationMinutes,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted swap mapping in status requested. */
fun newWeightedSwapMapping(code: String, label: String): WeightedSwapMapping {
    return WeightedSwapMapping(
        code = code,
        label = label,
        category = "",
        amount = 0.0,
        fuelLiters = 0.0,
        revision = 0,
        vehicleId = "",
        durationMinutes = 0,
        weight = 0.0,
        status = WeightedSwapMappingStatus.REQUESTED,
    )
}
