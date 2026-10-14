package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import com.acme.transport.kit.text.normalize

/** Where a fuel surcharge score is in its life cycle. */
enum class FuelSurchargeScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fuel surcharge score. Its code is unique within a tenant. */
data class FuelSurchargeScore(
    var code: String,
    var name: String,
    var weight: Double,
    var source: String,
    var carrier: String,
    var region: String,
    var country: String,
    var vehicleId: String,
    var status: FuelSurchargeScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeScoreStatus): Boolean {
        when (status) {
            FuelSurchargeScoreStatus.OPEN -> return next == FuelSurchargeScoreStatus.RESOLVED
            FuelSurchargeScoreStatus.IN_REVIEW -> return next == FuelSurchargeScoreStatus.REOPENED
            FuelSurchargeScoreStatus.RESOLVED -> {
                when (next) {
                    FuelSurchargeScoreStatus.ARCHIVED -> return true
                }
                return false
            }
            FuelSurchargeScoreStatus.REOPENED -> return next == FuelSurchargeScoreStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeScoreStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = weight

    fun snapshot(): FuelSurchargeScore {
        return FuelSurchargeScore(
            code = code,
            name = name,
            weight = weight,
            source = source,
            carrier = carrier,
            region = region,
            country = country,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a fuel surcharge score in status open. */
fun newFuelSurchargeScore(code: String, name: String): FuelSurchargeScore {
    return FuelSurchargeScore(
        code = code,
        name = name,
        weight = 0.0,
        source = "",
        carrier = "",
        region = "",
        country = "",
        vehicleId = "",
        status = FuelSurchargeScoreStatus.OPEN,
    )
}
