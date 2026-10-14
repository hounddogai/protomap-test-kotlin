package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted hold override is in its life cycle. */
enum class WeightedHoldOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted hold override. Its code is unique within a tenant. */
data class WeightedHoldOverride(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var phone: String,
    var channel: String,
    var floorValue: Double,
    var status: WeightedHoldOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedHoldOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedHoldOverrideStatus): Boolean {
        when (status) {
            WeightedHoldOverrideStatus.DRAFT -> {
                when (next) {
                    WeightedHoldOverrideStatus.ACTIVE -> return true
                }
                return false
            }
            WeightedHoldOverrideStatus.PENDING -> return next == WeightedHoldOverrideStatus.ACTIVE || next == WeightedHoldOverrideStatus.CLOSED
            WeightedHoldOverrideStatus.ACTIVE -> return next == WeightedHoldOverrideStatus.SUSPENDED
            WeightedHoldOverrideStatus.SUSPENDED -> {
                when (next) {
                    WeightedHoldOverrideStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedHoldOverrideStatus) {
        if (!canMoveTo(next)) {
            error("weighted hold override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted hold override"} $code"

    fun netVolume(): Double = round((floorValue + fuelLiters) * 100) / 100

    fun snapshot(): WeightedHoldOverride {
        return WeightedHoldOverride(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            phone = phone,
            channel = channel,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted hold override in status draft. */
fun newWeightedHoldOverride(code: String, name: String): WeightedHoldOverride {
    return WeightedHoldOverride(
        code = code,
        name = name,
        fuelLiters = 0.0,
        phone = "",
        channel = "",
        floorValue = 0.0,
        status = WeightedHoldOverrideStatus.DRAFT,
    )
}
