package com.acme.transport.customs.broker.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal broker group records of customs's broker work, and the rules, stores, and calculations around
// them.

/** Where a seasonal broker group is in its life cycle. */
enum class SeasonalBrokerGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One seasonal broker group, with what the planners need to act on it. */
data class SeasonalBrokerGroup(
    var code: String,
    var name: String,
    var dock: String,
    var distanceKm: Double,
    var budget: Double,
    var fuelLiters: Double,
    var capacity: Int,
    var contactEmail: String,
    var carrier: String,
    var status: SeasonalBrokerGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalBrokerGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalBrokerGroupStatus): Boolean {
        when (status) {
            SeasonalBrokerGroupStatus.DRAFT -> return next == SeasonalBrokerGroupStatus.ACTIVE
            SeasonalBrokerGroupStatus.PENDING -> {
                when (next) {
                    SeasonalBrokerGroupStatus.ACTIVE -> return true
                }
                return false
            }
            SeasonalBrokerGroupStatus.ACTIVE -> return next == SeasonalBrokerGroupStatus.SUSPENDED || next == SeasonalBrokerGroupStatus.CLOSED
            SeasonalBrokerGroupStatus.SUSPENDED -> {
                when (next) {
                    SeasonalBrokerGroupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalBrokerGroupStatus) {
        if (!canMoveTo(next)) {
            error("seasonal broker group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round((distanceKm + capacity.toDouble()) * 100) / 100

    fun snapshot(): SeasonalBrokerGroup {
        return SeasonalBrokerGroup(
            code = code,
            name = name,
            dock = dock,
            distanceKm = distanceKm,
            budget = budget,
            fuelLiters = fuelLiters,
            capacity = capacity,
            contactEmail = contactEmail,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal broker group in status draft. */
fun newSeasonalBrokerGroup(code: String, name: String): SeasonalBrokerGroup {
    return SeasonalBrokerGroup(
        code = code,
        name = name,
        dock = "",
        distanceKm = 0.0,
        budget = 0.0,
        fuelLiters = 0.0,
        capacity = 0,
        contactEmail = "",
        carrier = "",
        status = SeasonalBrokerGroupStatus.DRAFT,
    )
}
