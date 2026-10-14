package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dwell variance records of terminal's dwell work, and the rules, stores, and calculations around them.

/** Where a dwell variance is in its life cycle. */
enum class DwellVarianceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A dwell variance as the dwell team keeps it. */
data class DwellVariance(
    var code: String,
    var name: String,
    var limit: Int,
    var floorValue: Double,
    var amount: Double,
    var discount: Double,
    var phone: String,
    var vehicleId: String,
    var status: DwellVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellVarianceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellVarianceStatus): Boolean {
        when (status) {
            DwellVarianceStatus.REQUESTED -> {
                when (next) {
                    DwellVarianceStatus.GRANTED, DwellVarianceStatus.REVOKED -> return true
                }
                return false
            }
            DwellVarianceStatus.GRANTED -> return next == DwellVarianceStatus.REVOKED || next == DwellVarianceStatus.EXPIRED
            DwellVarianceStatus.REVOKED -> return next == DwellVarianceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DwellVarianceStatus) {
        if (!canMoveTo(next)) {
            error("dwell variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dwell variance"} $code"

    fun netExposure(): Double = round(floorValue * limit.toDouble() * 100) / 100

    fun snapshot(): DwellVariance {
        return DwellVariance(
            code = code,
            name = name,
            limit = limit,
            floorValue = floorValue,
            amount = amount,
            discount = discount,
            phone = phone,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dwell variance in status requested. */
fun newDwellVariance(code: String, name: String): DwellVariance {
    return DwellVariance(
        code = code,
        name = name,
        limit = 0,
        floorValue = 0.0,
        amount = 0.0,
        discount = 0.0,
        phone = "",
        vehicleId = "",
        status = DwellVarianceStatus.REQUESTED,
    )
}
