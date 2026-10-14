package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge digest is in its life cycle. */
enum class EvChargeDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a ev charge digest and its current status. */
data class EvChargeDigest(
    var code: String,
    var name: String,
    var discount: Double,
    var odometer: Int,
    var weight: Double,
    var region: String,
    var vehicleId: String,
    var currency: String,
    var visible: Boolean,
    var status: EvChargeDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeDigestStatus): Boolean {
        when (status) {
            EvChargeDigestStatus.IDLE -> return next == EvChargeDigestStatus.WARMING
            EvChargeDigestStatus.WARMING -> {
                when (next) {
                    EvChargeDigestStatus.READY, EvChargeDigestStatus.STOPPED -> return true
                }
                return false
            }
            EvChargeDigestStatus.READY -> return next == EvChargeDigestStatus.DRAINING
            EvChargeDigestStatus.DRAINING -> {
                when (next) {
                    EvChargeDigestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeDigestStatus) {
        if (!canMoveTo(next)) {
            error("ev charge digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"ev charge digest"} $code"

    fun weightedLoad(): Double = round((discount - weight) * 100) / 100

    fun snapshot(): EvChargeDigest {
        return EvChargeDigest(
            code = code,
            name = name,
            discount = discount,
            odometer = odometer,
            weight = weight,
            region = region,
            vehicleId = vehicleId,
            currency = currency,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a ev charge digest in status idle. */
fun newEvChargeDigest(code: String, name: String): EvChargeDigest {
    return EvChargeDigest(
        code = code,
        name = name,
        discount = 0.0,
        odometer = 0,
        weight = 0.0,
        region = "",
        vehicleId = "",
        currency = "",
        visible = false,
        status = EvChargeDigestStatus.IDLE,
    )
}
