package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal hub entry records of network's hub work, and the rules, stores, and calculations around them.

/** Where a seasonal hub entry is in its life cycle. */
enum class SeasonalHubEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a seasonal hub entry and its current status. */
data class SeasonalHubEntry(
    var code: String,
    var name: String,
    var budget: Double,
    var country: String,
    var capacity: Int,
    var locale: String,
    var status: SeasonalHubEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalHubEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalHubEntryStatus): Boolean {
        when (status) {
            SeasonalHubEntryStatus.REQUESTED -> return next == SeasonalHubEntryStatus.EXPIRED
            SeasonalHubEntryStatus.GRANTED -> return next == SeasonalHubEntryStatus.REVOKED
            SeasonalHubEntryStatus.REVOKED -> return next == SeasonalHubEntryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalHubEntryStatus) {
        if (!canMoveTo(next)) {
            error("seasonal hub entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedLoad(): Double = round((capacity.toDouble() - budget) * 100) / 100

    fun snapshot(): SeasonalHubEntry {
        return SeasonalHubEntry(
            code = code,
            name = name,
            budget = budget,
            country = country,
            capacity = capacity,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a seasonal hub entry in status requested. */
fun newSeasonalHubEntry(code: String, name: String): SeasonalHubEntry {
    return SeasonalHubEntry(
        code = code,
        name = name,
        budget = 0.0,
        country = "",
        capacity = 0,
        locale = "",
        status = SeasonalHubEntryStatus.REQUESTED,
    )
}
