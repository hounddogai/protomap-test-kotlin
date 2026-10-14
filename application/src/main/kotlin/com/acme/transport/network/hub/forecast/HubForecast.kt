package com.acme.transport.network.hub.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hub forecast records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub forecast is in its life cycle. */
enum class HubForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A hub forecast. Its code is unique within a tenant. */
data class HubForecast(
    var code: String,
    var name: String,
    var stopCount: Int,
    var archived: Boolean,
    var visible: Boolean,
    var source: String,
    var floorValue: Double,
    var channel: String,
    var status: HubForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: HubForecastStatus): Boolean {
        when (status) {
            HubForecastStatus.PROPOSED -> return next == HubForecastStatus.WITHDRAWN
            HubForecastStatus.APPROVED -> {
                when (next) {
                    HubForecastStatus.REJECTED, HubForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
            HubForecastStatus.REJECTED -> {
                when (next) {
                    HubForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubForecastStatus) {
        if (!canMoveTo(next)) {
            error("hub forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"hub forecast"} $code"

    fun weightedExposure(): Double = round(floorValue * stopCount.toDouble() * 100) / 100

    fun snapshot(): HubForecast {
        return HubForecast(
            code = code,
            name = name,
            stopCount = stopCount,
            archived = archived,
            visible = visible,
            source = source,
            floorValue = floorValue,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a hub forecast in status proposed. */
fun newHubForecast(code: String, name: String): HubForecast {
    return HubForecast(
        code = code,
        name = name,
        stopCount = 0,
        archived = false,
        visible = false,
        source = "",
        floorValue = 0.0,
        channel = "",
        status = HubForecastStatus.PROPOSED,
    )
}
