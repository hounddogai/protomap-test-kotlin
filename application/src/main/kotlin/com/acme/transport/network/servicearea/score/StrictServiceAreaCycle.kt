package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.text.normalize

/** Where a strict service area cycle is in its life cycle. */
enum class StrictServiceAreaCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One strict service area cycle, with what the planners need to act on it. */
data class StrictServiceAreaCycle(
    var code: String,
    var title: String,
    var region: String,
    var attributes: MutableMap<String, String>,
    var channel: String,
    var summary: String,
    var ratio: Double,
    var status: StrictServiceAreaCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictServiceAreaCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictServiceAreaCycleStatus): Boolean {
        when (status) {
            StrictServiceAreaCycleStatus.IDLE -> {
                when (next) {
                    StrictServiceAreaCycleStatus.READY, StrictServiceAreaCycleStatus.DRAINING -> return true
                }
                return false
            }
            StrictServiceAreaCycleStatus.WARMING -> return next == StrictServiceAreaCycleStatus.READY || next == StrictServiceAreaCycleStatus.STOPPED
            StrictServiceAreaCycleStatus.READY -> return next == StrictServiceAreaCycleStatus.STOPPED
            StrictServiceAreaCycleStatus.DRAINING -> return next == StrictServiceAreaCycleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictServiceAreaCycleStatus) {
        if (!canMoveTo(next)) {
            error("strict service area cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = ratio

    fun snapshot(): StrictServiceAreaCycle {
        return StrictServiceAreaCycle(
            code = code,
            title = title,
            region = region,
            attributes = attributes,
            channel = channel,
            summary = summary,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict service area cycle in status idle. */
fun newStrictServiceAreaCycle(code: String, title: String): StrictServiceAreaCycle {
    return StrictServiceAreaCycle(
        code = code,
        title = title,
        region = "",
        attributes = mutableMapOf<String, String>(),
        channel = "",
        summary = "",
        ratio = 0.0,
        status = StrictServiceAreaCycleStatus.IDLE,
    )
}
