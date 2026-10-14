package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay tier is in its life cycle. */
enum class RelayTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a relay tier and its current status. */
data class RelayTier(
    var code: String,
    var name: String,
    var externalRef: String,
    var driverId: String,
    var stopCount: Int,
    var active: Boolean,
    var cost: Double,
    var timezone: String,
    var visible: Boolean,
    var country: String,
    var depot: String,
    var status: RelayTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayTierStatus): Boolean {
        when (status) {
            RelayTierStatus.REQUESTED -> return next == RelayTierStatus.GRANTED || next == RelayTierStatus.REVOKED
            RelayTierStatus.GRANTED -> {
                when (next) {
                    RelayTierStatus.REVOKED, RelayTierStatus.EXPIRED -> return true
                }
                return false
            }
            RelayTierStatus.REVOKED -> return next == RelayTierStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RelayTierStatus) {
        if (!canMoveTo(next)) {
            error("relay tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveTotal(): Double = round(stopCount.toDouble() * cost * 100) / 100

    fun snapshot(): RelayTier {
        return RelayTier(
            code = code,
            name = name,
            externalRef = externalRef,
            driverId = driverId,
            stopCount = stopCount,
            active = active,
            cost = cost,
            timezone = timezone,
            visible = visible,
            country = country,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a relay tier in status requested. */
fun newRelayTier(code: String, name: String): RelayTier {
    return RelayTier(
        code = code,
        name = name,
        externalRef = "",
        driverId = "",
        stopCount = 0,
        active = false,
        cost = 0.0,
        timezone = "",
        visible = false,
        country = "",
        depot = "",
        status = RelayTierStatus.REQUESTED,
    )
}
