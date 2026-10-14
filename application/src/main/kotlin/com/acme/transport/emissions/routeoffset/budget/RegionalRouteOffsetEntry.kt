package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional route offset entry is in its life cycle. */
enum class RegionalRouteOffsetEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a regional route offset entry and its current status. */
data class RegionalRouteOffsetEntry(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var source: String,
    var endsAt: Int,
    var fuelLiters: Double,
    var trailerId: String,
    var amount: Double,
    var unit: String,
    var status: RegionalRouteOffsetEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRouteOffsetEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRouteOffsetEntryStatus): Boolean {
        when (status) {
            RegionalRouteOffsetEntryStatus.REQUESTED -> return next == RegionalRouteOffsetEntryStatus.REVOKED
            RegionalRouteOffsetEntryStatus.GRANTED -> return next == RegionalRouteOffsetEntryStatus.REVOKED || next == RegionalRouteOffsetEntryStatus.EXPIRED
            RegionalRouteOffsetEntryStatus.REVOKED -> {
                when (next) {
                    RegionalRouteOffsetEntryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalRouteOffsetEntryStatus) {
        if (!canMoveTo(next)) {
            error("regional route offset entry $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netVolume(): Double = round(amount * endsAt.toDouble() * 100) / 100

    fun snapshot(): RegionalRouteOffsetEntry {
        return RegionalRouteOffsetEntry(
            code = code,
            name = name,
            pinned = pinned,
            source = source,
            endsAt = endsAt,
            fuelLiters = fuelLiters,
            trailerId = trailerId,
            amount = amount,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional route offset entry in status requested. */
fun newRegionalRouteOffsetEntry(code: String, name: String): RegionalRouteOffsetEntry {
    return RegionalRouteOffsetEntry(
        code = code,
        name = name,
        pinned = false,
        source = "",
        endsAt = 0,
        fuelLiters = 0.0,
        trailerId = "",
        amount = 0.0,
        unit = "",
        status = RegionalRouteOffsetEntryStatus.REQUESTED,
    )
}
