package com.acme.transport.network.hub.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hub metric records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub metric is in its life cycle. */
enum class HubMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A hub metric. Its code is unique within a tenant. */
data class HubMetric(
    var code: String,
    var name: String,
    var revision: Int,
    var locale: String,
    var approved: Boolean,
    var priority: Int,
    var ceiling: Double,
    var tags: MutableList<String>,
    var ownerId: String,
    var lane: String,
    var timezone: String,
    var status: HubMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubMetricStatus): Boolean {
        when (status) {
            HubMetricStatus.IDLE -> {
                when (next) {
                    HubMetricStatus.DRAINING, HubMetricStatus.STOPPED -> return true
                }
                return false
            }
            HubMetricStatus.WARMING -> return next == HubMetricStatus.READY || next == HubMetricStatus.STOPPED
            HubMetricStatus.READY -> {
                when (next) {
                    HubMetricStatus.DRAINING -> return true
                }
                return false
            }
            HubMetricStatus.DRAINING -> {
                when (next) {
                    HubMetricStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubMetricStatus) {
        if (!canMoveTo(next)) {
            error("hub metric $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round((priority.toDouble() - ceiling) * 100) / 100

    fun snapshot(): HubMetric {
        return HubMetric(
            code = code,
            name = name,
            revision = revision,
            locale = locale,
            approved = approved,
            priority = priority,
            ceiling = ceiling,
            tags = tags,
            ownerId = ownerId,
            lane = lane,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a hub metric in status idle. */
fun newHubMetric(code: String, name: String): HubMetric {
    return HubMetric(
        code = code,
        name = name,
        revision = 0,
        locale = "",
        approved = false,
        priority = 0,
        ceiling = 0.0,
        tags = mutableListOf<String>(),
        ownerId = "",
        lane = "",
        timezone = "",
        status = HubMetricStatus.IDLE,
    )
}
