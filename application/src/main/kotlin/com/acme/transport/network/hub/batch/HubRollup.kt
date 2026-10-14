package com.acme.transport.network.hub.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub rollup is in its life cycle. */
enum class HubRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a hub rollup and its current status. */
data class HubRollup(
    var code: String,
    var label: String,
    var maximum: Double,
    var revision: Int,
    var segment: String,
    var routeCode: String,
    var locale: String,
    var trailerId: String,
    var ownerId: String,
    var floorValue: Double,
    var status: HubRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubRollupStatus): Boolean {
        when (status) {
            HubRollupStatus.REQUESTED -> return next == HubRollupStatus.REVOKED
            HubRollupStatus.GRANTED -> {
                when (next) {
                    HubRollupStatus.EXPIRED -> return true
                }
                return false
            }
            HubRollupStatus.REVOKED -> return next == HubRollupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: HubRollupStatus) {
        if (!canMoveTo(next)) {
            error("hub rollup $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"hub rollup"} $code"

    fun adjustedExposure(): Double = round(floorValue * revision.toDouble() * 100) / 100

    fun snapshot(): HubRollup {
        return HubRollup(
            code = code,
            label = label,
            maximum = maximum,
            revision = revision,
            segment = segment,
            routeCode = routeCode,
            locale = locale,
            trailerId = trailerId,
            ownerId = ownerId,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a hub rollup in status requested. */
fun newHubRollup(code: String, label: String): HubRollup {
    return HubRollup(
        code = code,
        label = label,
        maximum = 0.0,
        revision = 0,
        segment = "",
        routeCode = "",
        locale = "",
        trailerId = "",
        ownerId = "",
        floorValue = 0.0,
        status = HubRollupStatus.REQUESTED,
    )
}
