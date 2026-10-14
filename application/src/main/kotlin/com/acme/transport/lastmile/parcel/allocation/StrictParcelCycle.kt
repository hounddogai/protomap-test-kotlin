package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict parcel cycle is in its life cycle. */
enum class StrictParcelCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A strict parcel cycle as the parcel team keeps it. */
data class StrictParcelCycle(
    var code: String,
    var name: String,
    var driverId: String,
    var reviewer: String,
    var score: Double,
    var batchSize: Int,
    var summary: String,
    var currency: String,
    var windowDays: Int,
    var tags: MutableList<String>,
    var capacity: Int,
    var status: StrictParcelCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictParcelCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictParcelCycleStatus): Boolean {
        when (status) {
            StrictParcelCycleStatus.PROPOSED -> {
                when (next) {
                    StrictParcelCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictParcelCycleStatus.APPROVED -> return next == StrictParcelCycleStatus.REJECTED || next == StrictParcelCycleStatus.WITHDRAWN
            StrictParcelCycleStatus.REJECTED -> {
                when (next) {
                    StrictParcelCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictParcelCycleStatus) {
        if (!canMoveTo(next)) {
            error("strict parcel cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round(capacity.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): StrictParcelCycle {
        return StrictParcelCycle(
            code = code,
            name = name,
            driverId = driverId,
            reviewer = reviewer,
            score = score,
            batchSize = batchSize,
            summary = summary,
            currency = currency,
            windowDays = windowDays,
            tags = tags,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict parcel cycle in status proposed. */
fun newStrictParcelCycle(code: String, name: String): StrictParcelCycle {
    return StrictParcelCycle(
        code = code,
        name = name,
        driverId = "",
        reviewer = "",
        score = 0.0,
        batchSize = 0,
        summary = "",
        currency = "",
        windowDays = 0,
        tags = mutableListOf<String>(),
        capacity = 0,
        status = StrictParcelCycleStatus.PROPOSED,
    )
}
