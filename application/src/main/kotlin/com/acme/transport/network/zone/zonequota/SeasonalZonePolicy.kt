package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.text.normalize

/** Where a seasonal zone policy is in its life cycle. */
enum class SeasonalZonePolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A seasonal zone policy. Its code is unique within a tenant. */
data class SeasonalZonePolicy(
    var code: String,
    var name: String,
    var active: Boolean,
    var summary: String,
    var routeCode: String,
    var trailerId: String,
    var amount: Double,
    var status: SeasonalZonePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalZonePolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalZonePolicyStatus): Boolean {
        when (status) {
            SeasonalZonePolicyStatus.CREATED -> return next == SeasonalZonePolicyStatus.PUBLISHED
            SeasonalZonePolicyStatus.VALIDATED -> {
                when (next) {
                    SeasonalZonePolicyStatus.PUBLISHED, SeasonalZonePolicyStatus.RETIRED -> return true
                }
                return false
            }
            SeasonalZonePolicyStatus.PUBLISHED -> return next == SeasonalZonePolicyStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SeasonalZonePolicyStatus) {
        if (!canMoveTo(next)) {
            error("seasonal zone policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = amount

    fun snapshot(): SeasonalZonePolicy {
        return SeasonalZonePolicy(
            code = code,
            name = name,
            active = active,
            summary = summary,
            routeCode = routeCode,
            trailerId = trailerId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal zone policy in status created. */
fun newSeasonalZonePolicy(code: String, name: String): SeasonalZonePolicy {
    return SeasonalZonePolicy(
        code = code,
        name = name,
        active = false,
        summary = "",
        routeCode = "",
        trailerId = "",
        amount = 0.0,
        status = SeasonalZonePolicyStatus.CREATED,
    )
}
