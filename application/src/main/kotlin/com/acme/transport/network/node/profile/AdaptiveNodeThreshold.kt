package com.acme.transport.network.node.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive node threshold is in its life cycle. */
enum class AdaptiveNodeThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive node threshold. Its code is unique within a tenant. */
data class AdaptiveNodeThreshold(
    var code: String,
    var name: String,
    var locale: String,
    var margin: Double,
    var discount: Double,
    var odometer: Int,
    var fuelLiters: Double,
    var title: String,
    var driverId: String,
    var lane: String,
    var currency: String,
    var status: AdaptiveNodeThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveNodeThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveNodeThresholdStatus): Boolean {
        when (status) {
            AdaptiveNodeThresholdStatus.IDLE -> {
                when (next) {
                    AdaptiveNodeThresholdStatus.WARMING -> return true
                }
                return false
            }
            AdaptiveNodeThresholdStatus.WARMING -> {
                when (next) {
                    AdaptiveNodeThresholdStatus.READY, AdaptiveNodeThresholdStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveNodeThresholdStatus.READY -> {
                when (next) {
                    AdaptiveNodeThresholdStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveNodeThresholdStatus.DRAINING -> {
                when (next) {
                    AdaptiveNodeThresholdStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveNodeThresholdStatus) {
        if (!canMoveTo(next)) {
            error("adaptive node threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive node threshold"} $code"

    fun netVolume(): Double = round((discount + odometer.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveNodeThreshold {
        return AdaptiveNodeThreshold(
            code = code,
            name = name,
            locale = locale,
            margin = margin,
            discount = discount,
            odometer = odometer,
            fuelLiters = fuelLiters,
            title = title,
            driverId = driverId,
            lane = lane,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive node threshold in status idle. */
fun newAdaptiveNodeThreshold(code: String, name: String): AdaptiveNodeThreshold {
    return AdaptiveNodeThreshold(
        code = code,
        name = name,
        locale = "",
        margin = 0.0,
        discount = 0.0,
        odometer = 0,
        fuelLiters = 0.0,
        title = "",
        driverId = "",
        lane = "",
        currency = "",
        status = AdaptiveNodeThresholdStatus.IDLE,
    )
}
