package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel burn request records of emissions's fuel burn work, and the rules, stores, and calculations around
// them.

/** Where a fuel burn request is in its life cycle. */
enum class FuelBurnRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fuel burn request as the fuel burn team keeps it. */
data class FuelBurnRequest(
    var code: String,
    var name: String,
    var amount: Double,
    var batchSize: Int,
    var driverId: String,
    var region: String,
    var createdAt: Int,
    var locale: String,
    var status: FuelBurnRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnRequestStatus): Boolean {
        when (status) {
            FuelBurnRequestStatus.PROPOSED -> {
                when (next) {
                    FuelBurnRequestStatus.APPROVED, FuelBurnRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
            FuelBurnRequestStatus.APPROVED -> return next == FuelBurnRequestStatus.REJECTED || next == FuelBurnRequestStatus.WITHDRAWN
            FuelBurnRequestStatus.REJECTED -> {
                when (next) {
                    FuelBurnRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnRequestStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn request $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fuel burn request"} $code"

    fun effectiveVolume(): Double = round((amount - batchSize.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnRequest {
        return FuelBurnRequest(
            code = code,
            name = name,
            amount = amount,
            batchSize = batchSize,
            driverId = driverId,
            region = region,
            createdAt = createdAt,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fuel burn request in status proposed. */
fun newFuelBurnRequest(code: String, name: String): FuelBurnRequest {
    return FuelBurnRequest(
        code = code,
        name = name,
        amount = 0.0,
        batchSize = 0,
        driverId = "",
        region = "",
        createdAt = 0,
        locale = "",
        status = FuelBurnRequestStatus.PROPOSED,
    )
}
