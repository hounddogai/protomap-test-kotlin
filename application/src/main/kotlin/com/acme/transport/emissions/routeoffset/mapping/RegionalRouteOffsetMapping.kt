package com.acme.transport.emissions.routeoffset.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional route offset mapping records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a regional route offset mapping is in its life cycle. */
enum class RegionalRouteOffsetMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a regional route offset mapping and its current status. */
data class RegionalRouteOffsetMapping(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var endsAt: Int,
    var vehicleId: String,
    var timezone: String,
    var phone: String,
    var distanceKm: Double,
    var source: String,
    var lane: String,
    var status: RegionalRouteOffsetMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRouteOffsetMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRouteOffsetMappingStatus): Boolean {
        when (status) {
            RegionalRouteOffsetMappingStatus.REQUESTED -> return next == RegionalRouteOffsetMappingStatus.REVOKED || next == RegionalRouteOffsetMappingStatus.EXPIRED
            RegionalRouteOffsetMappingStatus.GRANTED -> return next == RegionalRouteOffsetMappingStatus.REVOKED || next == RegionalRouteOffsetMappingStatus.EXPIRED
            RegionalRouteOffsetMappingStatus.REVOKED -> return next == RegionalRouteOffsetMappingStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalRouteOffsetMappingStatus) {
        if (!canMoveTo(next)) {
            error("regional route offset mapping $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedTotal(): Double = round((distanceKm + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalRouteOffsetMapping {
        return RegionalRouteOffsetMapping(
            code = code,
            title = title,
            expiresAt = expiresAt,
            endsAt = endsAt,
            vehicleId = vehicleId,
            timezone = timezone,
            phone = phone,
            distanceKm = distanceKm,
            source = source,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional route offset mapping in status requested. */
fun newRegionalRouteOffsetMapping(code: String, title: String): RegionalRouteOffsetMapping {
    return RegionalRouteOffsetMapping(
        code = code,
        title = title,
        expiresAt = 0,
        endsAt = 0,
        vehicleId = "",
        timezone = "",
        phone = "",
        distanceKm = 0.0,
        source = "",
        lane = "",
        status = RegionalRouteOffsetMappingStatus.REQUESTED,
    )
}
