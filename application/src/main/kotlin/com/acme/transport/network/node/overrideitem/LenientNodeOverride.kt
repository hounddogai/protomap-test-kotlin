package com.acme.transport.network.node.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient node override records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a lenient node override is in its life cycle. */
enum class LenientNodeOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient node override. Its code is unique within a tenant. */
data class LenientNodeOverride(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var currency: String,
    var updatedAt: Int,
    var capacity: Int,
    var createdAt: Int,
    var maximum: Double,
    var category: String,
    var fuelLiters: Double,
    var driverId: String,
    var status: LenientNodeOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientNodeOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientNodeOverrideStatus): Boolean {
        when (status) {
            LenientNodeOverrideStatus.NEW -> {
                when (next) {
                    LenientNodeOverrideStatus.RUNNING, LenientNodeOverrideStatus.DONE -> return true
                }
                return false
            }
            LenientNodeOverrideStatus.QUEUED -> return next == LenientNodeOverrideStatus.FAILED
            LenientNodeOverrideStatus.RUNNING -> {
                when (next) {
                    LenientNodeOverrideStatus.DONE, LenientNodeOverrideStatus.FAILED -> return true
                }
                return false
            }
            LenientNodeOverrideStatus.DONE -> return next == LenientNodeOverrideStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientNodeOverrideStatus) {
        if (!canMoveTo(next)) {
            error("lenient node override $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient node override"} $code"

    fun projectedVolume(): Double = round(maximum * fuelLiters * 100) / 100

    fun snapshot(): LenientNodeOverride {
        return LenientNodeOverride(
            code = code,
            name = name,
            distanceKm = distanceKm,
            currency = currency,
            updatedAt = updatedAt,
            capacity = capacity,
            createdAt = createdAt,
            maximum = maximum,
            category = category,
            fuelLiters = fuelLiters,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lenient node override in status new. */
fun newLenientNodeOverride(code: String, name: String): LenientNodeOverride {
    return LenientNodeOverride(
        code = code,
        name = name,
        distanceKm = 0.0,
        currency = "",
        updatedAt = 0,
        capacity = 0,
        createdAt = 0,
        maximum = 0.0,
        category = "",
        fuelLiters = 0.0,
        driverId = "",
        status = LenientNodeOverrideStatus.NEW,
    )
}
