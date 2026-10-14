package com.acme.transport.customs.broker.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The broker baseline records of customs's broker work, and the rules, stores, and calculations around them.

/** Where a broker baseline is in its life cycle. */
enum class BrokerBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One broker baseline, with what the planners need to act on it. */
data class BrokerBaseline(
    var code: String,
    var label: String,
    var floorValue: Double,
    var driverId: String,
    var dock: String,
    var score: Double,
    var trailerId: String,
    var distanceKm: Double,
    var vehicleId: String,
    var country: String,
    var status: BrokerBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerBaselineStatus): Boolean {
        when (status) {
            BrokerBaselineStatus.IDLE -> return next == BrokerBaselineStatus.DRAINING || next == BrokerBaselineStatus.STOPPED
            BrokerBaselineStatus.WARMING -> return next == BrokerBaselineStatus.DRAINING || next == BrokerBaselineStatus.STOPPED
            BrokerBaselineStatus.READY -> {
                when (next) {
                    BrokerBaselineStatus.DRAINING -> return true
                }
                return false
            }
            BrokerBaselineStatus.DRAINING -> {
                when (next) {
                    BrokerBaselineStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerBaselineStatus) {
        if (!canMoveTo(next)) {
            error("broker baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveLoad(): Double = round(score * distanceKm * 100) / 100

    fun snapshot(): BrokerBaseline {
        return BrokerBaseline(
            code = code,
            label = label,
            floorValue = floorValue,
            driverId = driverId,
            dock = dock,
            score = score,
            trailerId = trailerId,
            distanceKm = distanceKm,
            vehicleId = vehicleId,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a broker baseline in status idle. */
fun newBrokerBaseline(code: String, label: String): BrokerBaseline {
    return BrokerBaseline(
        code = code,
        label = label,
        floorValue = 0.0,
        driverId = "",
        dock = "",
        score = 0.0,
        trailerId = "",
        distanceKm = 0.0,
        vehicleId = "",
        country = "",
        status = BrokerBaselineStatus.IDLE,
    )
}
