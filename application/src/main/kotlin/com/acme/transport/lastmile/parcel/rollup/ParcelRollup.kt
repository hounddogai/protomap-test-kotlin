package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The parcel rollup records of lastmile's parcel work, and the rules, stores, and calculations around them.

/** Where a parcel rollup is in its life cycle. */
enum class ParcelRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One parcel rollup, with what the planners need to act on it. */
data class ParcelRollup(
    var code: String,
    var label: String,
    var vehicleId: String,
    var budget: Double,
    var threshold: Double,
    var timezone: String,
    var windowDays: Int,
    var driverId: String,
    var category: String,
    var phone: String,
    var quantity: Int,
    var status: ParcelRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelRollupStatus): Boolean {
        when (status) {
            ParcelRollupStatus.IDLE -> return next == ParcelRollupStatus.WARMING || next == ParcelRollupStatus.READY
            ParcelRollupStatus.WARMING -> return next == ParcelRollupStatus.READY || next == ParcelRollupStatus.STOPPED
            ParcelRollupStatus.READY -> {
                when (next) {
                    ParcelRollupStatus.DRAINING, ParcelRollupStatus.STOPPED -> return true
                }
                return false
            }
            ParcelRollupStatus.DRAINING -> {
                when (next) {
                    ParcelRollupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelRollupStatus) {
        if (!canMoveTo(next)) {
            error("parcel rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round((threshold + quantity.toDouble()) * 100) / 100

    fun snapshot(): ParcelRollup {
        return ParcelRollup(
            code = code,
            label = label,
            vehicleId = vehicleId,
            budget = budget,
            threshold = threshold,
            timezone = timezone,
            windowDays = windowDays,
            driverId = driverId,
            category = category,
            phone = phone,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a parcel rollup in status idle. */
fun newParcelRollup(code: String, label: String): ParcelRollup {
    return ParcelRollup(
        code = code,
        label = label,
        vehicleId = "",
        budget = 0.0,
        threshold = 0.0,
        timezone = "",
        windowDays = 0,
        driverId = "",
        category = "",
        phone = "",
        quantity = 0,
        status = ParcelRollupStatus.IDLE,
    )
}
