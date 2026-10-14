package com.acme.transport.network.hub.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub threshold is in its life cycle. */
enum class HubThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A hub threshold as the hub team keeps it. */
data class HubThreshold(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var approved: Boolean,
    var etaMinutes: Int,
    var timezone: String,
    var carrier: String,
    var routeCode: String,
    var driverId: String,
    var status: HubThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubThresholdStatus): Boolean {
        when (status) {
            HubThresholdStatus.REQUESTED -> {
                when (next) {
                    HubThresholdStatus.GRANTED, HubThresholdStatus.REVOKED -> return true
                }
                return false
            }
            HubThresholdStatus.GRANTED -> {
                when (next) {
                    HubThresholdStatus.REVOKED, HubThresholdStatus.EXPIRED -> return true
                }
                return false
            }
            HubThresholdStatus.REVOKED -> return next == HubThresholdStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: HubThresholdStatus) {
        if (!canMoveTo(next)) {
            error("hub threshold $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"hub threshold"} $code"

    fun weightedLoad(): Double = round(expiresAt.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): HubThreshold {
        return HubThreshold(
            code = code,
            name = name,
            expiresAt = expiresAt,
            approved = approved,
            etaMinutes = etaMinutes,
            timezone = timezone,
            carrier = carrier,
            routeCode = routeCode,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hub threshold in status requested. */
fun newHubThreshold(code: String, name: String): HubThreshold {
    return HubThreshold(
        code = code,
        name = name,
        expiresAt = 0,
        approved = false,
        etaMinutes = 0,
        timezone = "",
        carrier = "",
        routeCode = "",
        driverId = "",
        status = HubThresholdStatus.REQUESTED,
    )
}
