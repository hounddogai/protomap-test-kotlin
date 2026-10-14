package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default near miss cycle records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a default near miss cycle is in its life cycle. */
enum class DefaultNearMissCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a default near miss cycle and its current status. */
data class DefaultNearMissCycle(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var maximum: Double,
    var quantity: Int,
    var version: Int,
    var summary: String,
    var source: String,
    var timezone: String,
    var status: DefaultNearMissCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultNearMissCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultNearMissCycleStatus): Boolean {
        when (status) {
            DefaultNearMissCycleStatus.IDLE -> {
                when (next) {
                    DefaultNearMissCycleStatus.STOPPED -> return true
                }
                return false
            }
            DefaultNearMissCycleStatus.WARMING -> return next == DefaultNearMissCycleStatus.DRAINING
            DefaultNearMissCycleStatus.READY -> return next == DefaultNearMissCycleStatus.DRAINING
            DefaultNearMissCycleStatus.DRAINING -> {
                when (next) {
                    DefaultNearMissCycleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultNearMissCycleStatus) {
        if (!canMoveTo(next)) {
            error("default near miss cycle $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun weightedVolume(): Double = round(maximum * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): DefaultNearMissCycle {
        return DefaultNearMissCycle(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            maximum = maximum,
            quantity = quantity,
            version = version,
            summary = summary,
            source = source,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a default near miss cycle in status idle. */
fun newDefaultNearMissCycle(code: String, name: String): DefaultNearMissCycle {
    return DefaultNearMissCycle(
        code = code,
        name = name,
        etaMinutes = 0,
        maximum = 0.0,
        quantity = 0,
        version = 0,
        summary = "",
        source = "",
        timezone = "",
        status = DefaultNearMissCycleStatus.IDLE,
    )
}
