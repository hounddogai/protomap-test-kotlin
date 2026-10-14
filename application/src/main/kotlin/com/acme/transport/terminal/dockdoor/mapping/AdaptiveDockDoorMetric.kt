package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive dock door metric is in its life cycle. */
enum class AdaptiveDockDoorMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive dock door metric as the dock door team keeps it. */
data class AdaptiveDockDoorMetric(
    var code: String,
    var name: String,
    var carrier: String,
    var version: Int,
    var dock: String,
    var timezone: String,
    var cost: Double,
    var etaMinutes: Int,
    var attributes: MutableMap<String, String>,
    var active: Boolean,
    var windowDays: Int,
    var status: AdaptiveDockDoorMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDockDoorMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDockDoorMetricStatus): Boolean {
        when (status) {
            AdaptiveDockDoorMetricStatus.IDLE -> return next == AdaptiveDockDoorMetricStatus.WARMING || next == AdaptiveDockDoorMetricStatus.DRAINING
            AdaptiveDockDoorMetricStatus.WARMING -> {
                when (next) {
                    AdaptiveDockDoorMetricStatus.READY, AdaptiveDockDoorMetricStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveDockDoorMetricStatus.READY -> {
                when (next) {
                    AdaptiveDockDoorMetricStatus.DRAINING, AdaptiveDockDoorMetricStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveDockDoorMetricStatus.DRAINING -> {
                when (next) {
                    AdaptiveDockDoorMetricStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDockDoorMetricStatus) {
        if (!canMoveTo(next)) {
            error("adaptive dock door metric $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netExposure(): Double = round(etaMinutes.toDouble() * cost * 100) / 100

    fun snapshot(): AdaptiveDockDoorMetric {
        return AdaptiveDockDoorMetric(
            code = code,
            name = name,
            carrier = carrier,
            version = version,
            dock = dock,
            timezone = timezone,
            cost = cost,
            etaMinutes = etaMinutes,
            attributes = attributes,
            active = active,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive dock door metric in status idle. */
fun newAdaptiveDockDoorMetric(code: String, name: String): AdaptiveDockDoorMetric {
    return AdaptiveDockDoorMetric(
        code = code,
        name = name,
        carrier = "",
        version = 0,
        dock = "",
        timezone = "",
        cost = 0.0,
        etaMinutes = 0,
        attributes = mutableMapOf<String, String>(),
        active = false,
        windowDays = 0,
        status = AdaptiveDockDoorMetricStatus.IDLE,
    )
}
