package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global trailer cycle records of linehaul's trailer work, and the rules, stores, and calculations around
// them.

/** Where a global trailer cycle is in its life cycle. */
enum class GlobalTrailerCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One global trailer cycle, with what the planners need to act on it. */
data class GlobalTrailerCycle(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var attempts: Int,
    var attributes: MutableMap<String, String>,
    var country: String,
    var status: GlobalTrailerCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTrailerCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTrailerCycleStatus): Boolean {
        when (status) {
            GlobalTrailerCycleStatus.CREATED -> {
                when (next) {
                    GlobalTrailerCycleStatus.PUBLISHED -> return true
                }
                return false
            }
            GlobalTrailerCycleStatus.VALIDATED -> return next == GlobalTrailerCycleStatus.PUBLISHED || next == GlobalTrailerCycleStatus.RETIRED
            GlobalTrailerCycleStatus.PUBLISHED -> {
                when (next) {
                    GlobalTrailerCycleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalTrailerCycleStatus) {
        if (!canMoveTo(next)) {
            error("global trailer cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global trailer cycle"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((distanceKm - attempts.toDouble()) * 100) / 100

    fun snapshot(): GlobalTrailerCycle {
        return GlobalTrailerCycle(
            code = code,
            name = name,
            distanceKm = distanceKm,
            attempts = attempts,
            attributes = attributes,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global trailer cycle in status created. */
fun newGlobalTrailerCycle(code: String, name: String): GlobalTrailerCycle {
    return GlobalTrailerCycle(
        code = code,
        name = name,
        distanceKm = 0.0,
        attempts = 0,
        attributes = mutableMapOf<String, String>(),
        country = "",
        status = GlobalTrailerCycleStatus.CREATED,
    )
}
