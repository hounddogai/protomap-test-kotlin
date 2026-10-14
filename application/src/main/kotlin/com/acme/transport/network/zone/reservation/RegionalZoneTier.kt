package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional zone tier is in its life cycle. */
enum class RegionalZoneTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a regional zone tier and its current status. */
data class RegionalZoneTier(
    var code: String,
    var label: String,
    var segment: String,
    var retries: Int,
    var amount: Double,
    var durationMinutes: Int,
    var timezone: String,
    var routeCode: String,
    var status: RegionalZoneTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalZoneTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalZoneTierStatus): Boolean {
        when (status) {
            RegionalZoneTierStatus.REQUESTED -> return next == RegionalZoneTierStatus.EXPIRED
            RegionalZoneTierStatus.GRANTED -> return next == RegionalZoneTierStatus.REVOKED || next == RegionalZoneTierStatus.EXPIRED
            RegionalZoneTierStatus.REVOKED -> return next == RegionalZoneTierStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalZoneTierStatus) {
        if (!canMoveTo(next)) {
            error("regional zone tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional zone tier"} $code"

    fun netExposure(): Double = round((durationMinutes.toDouble() - amount) * 100) / 100

    fun snapshot(): RegionalZoneTier {
        return RegionalZoneTier(
            code = code,
            label = label,
            segment = segment,
            retries = retries,
            amount = amount,
            durationMinutes = durationMinutes,
            timezone = timezone,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a regional zone tier in status requested. */
fun newRegionalZoneTier(code: String, label: String): RegionalZoneTier {
    return RegionalZoneTier(
        code = code,
        label = label,
        segment = "",
        retries = 0,
        amount = 0.0,
        durationMinutes = 0,
        timezone = "",
        routeCode = "",
        status = RegionalZoneTierStatus.REQUESTED,
    )
}
