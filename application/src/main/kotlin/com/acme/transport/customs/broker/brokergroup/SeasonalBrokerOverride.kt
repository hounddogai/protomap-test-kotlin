package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal broker override is in its life cycle. */
enum class SeasonalBrokerOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One seasonal broker override, with what the planners need to act on it. */
data class SeasonalBrokerOverride(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var currency: String,
    var capacity: Int,
    var status: SeasonalBrokerOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalBrokerOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalBrokerOverrideStatus): Boolean {
        when (status) {
            SeasonalBrokerOverrideStatus.CREATED -> {
                when (next) {
                    SeasonalBrokerOverrideStatus.VALIDATED, SeasonalBrokerOverrideStatus.PUBLISHED -> return true
                }
                return false
            }
            SeasonalBrokerOverrideStatus.VALIDATED -> return next == SeasonalBrokerOverrideStatus.RETIRED
            SeasonalBrokerOverrideStatus.PUBLISHED -> {
                when (next) {
                    SeasonalBrokerOverrideStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalBrokerOverrideStatus) {
        if (!canMoveTo(next)) {
            error("seasonal broker override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedLoad(): Double = round((capacity.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): SeasonalBrokerOverride {
        return SeasonalBrokerOverride(
            code = code,
            name = name,
            distanceKm = distanceKm,
            currency = currency,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal broker override in status created. */
fun newSeasonalBrokerOverride(code: String, name: String): SeasonalBrokerOverride {
    return SeasonalBrokerOverride(
        code = code,
        name = name,
        distanceKm = 0.0,
        currency = "",
        capacity = 0,
        status = SeasonalBrokerOverrideStatus.CREATED,
    )
}
