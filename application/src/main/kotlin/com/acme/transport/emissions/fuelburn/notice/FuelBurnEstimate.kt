package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.kit.text.normalize

/** Where a fuel burn estimate is in its life cycle. */
enum class FuelBurnEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fuel burn estimate as the fuel burn team keeps it. */
data class FuelBurnEstimate(
    var code: String,
    var title: String,
    var archived: Boolean,
    var attributes: MutableMap<String, String>,
    var maximum: Double,
    var currency: String,
    var vehicleId: String,
    var status: FuelBurnEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnEstimateStatus): Boolean {
        when (status) {
            FuelBurnEstimateStatus.PROPOSED -> return next == FuelBurnEstimateStatus.REJECTED
            FuelBurnEstimateStatus.APPROVED -> {
                when (next) {
                    FuelBurnEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
            FuelBurnEstimateStatus.REJECTED -> {
                when (next) {
                    FuelBurnEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnEstimateStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = maximum

    fun snapshot(): FuelBurnEstimate {
        return FuelBurnEstimate(
            code = code,
            title = title,
            archived = archived,
            attributes = attributes,
            maximum = maximum,
            currency = currency,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel burn estimate in status proposed. */
fun newFuelBurnEstimate(code: String, title: String): FuelBurnEstimate {
    return FuelBurnEstimate(
        code = code,
        title = title,
        archived = false,
        attributes = mutableMapOf<String, String>(),
        maximum = 0.0,
        currency = "",
        vehicleId = "",
        status = FuelBurnEstimateStatus.PROPOSED,
    )
}
