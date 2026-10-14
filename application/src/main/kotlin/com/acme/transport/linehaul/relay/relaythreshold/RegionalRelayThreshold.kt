package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional relay threshold records of linehaul's relay work, and the rules, stores, and calculations
// around them.

/** Where a regional relay threshold is in its life cycle. */
enum class RegionalRelayThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a regional relay threshold and its current status. */
data class RegionalRelayThreshold(
    var code: String,
    var name: String,
    var dock: String,
    var driverId: String,
    var createdAt: Int,
    var visible: Boolean,
    var channel: String,
    var retries: Int,
    var startsAt: Int,
    var status: RegionalRelayThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRelayThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRelayThresholdStatus): Boolean {
        when (status) {
            RegionalRelayThresholdStatus.REQUESTED -> {
                when (next) {
                    RegionalRelayThresholdStatus.GRANTED, RegionalRelayThresholdStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalRelayThresholdStatus.GRANTED -> {
                when (next) {
                    RegionalRelayThresholdStatus.REVOKED -> return true
                }
                return false
            }
            RegionalRelayThresholdStatus.REVOKED -> {
                when (next) {
                    RegionalRelayThresholdStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalRelayThresholdStatus) {
        if (!canMoveTo(next)) {
            error("regional relay threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedLoad(): Double = round((startsAt.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): RegionalRelayThreshold {
        return RegionalRelayThreshold(
            code = code,
            name = name,
            dock = dock,
            driverId = driverId,
            createdAt = createdAt,
            visible = visible,
            channel = channel,
            retries = retries,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional relay threshold in status requested. */
fun newRegionalRelayThreshold(code: String, name: String): RegionalRelayThreshold {
    return RegionalRelayThreshold(
        code = code,
        name = name,
        dock = "",
        driverId = "",
        createdAt = 0,
        visible = false,
        channel = "",
        retries = 0,
        startsAt = 0,
        status = RegionalRelayThresholdStatus.REQUESTED,
    )
}
