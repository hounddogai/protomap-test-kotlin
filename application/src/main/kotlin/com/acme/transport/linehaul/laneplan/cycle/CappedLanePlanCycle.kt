package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped lane plan cycle records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a capped lane plan cycle is in its life cycle. */
enum class CappedLanePlanCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped lane plan cycle as the lane plan team keeps it. */
data class CappedLanePlanCycle(
    var code: String,
    var title: String,
    var stopCount: Int,
    var attributes: MutableMap<String, String>,
    var reviewer: String,
    var distanceKm: Double,
    var status: CappedLanePlanCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedLanePlanCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedLanePlanCycleStatus): Boolean {
        when (status) {
            CappedLanePlanCycleStatus.REQUESTED -> return next == CappedLanePlanCycleStatus.GRANTED || next == CappedLanePlanCycleStatus.EXPIRED
            CappedLanePlanCycleStatus.GRANTED -> return next == CappedLanePlanCycleStatus.REVOKED || next == CappedLanePlanCycleStatus.EXPIRED
            CappedLanePlanCycleStatus.REVOKED -> return next == CappedLanePlanCycleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedLanePlanCycleStatus) {
        if (!canMoveTo(next)) {
            error("capped lane plan cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped lane plan cycle"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedExposure(): Double = round(distanceKm * stopCount.toDouble() * 100) / 100

    fun snapshot(): CappedLanePlanCycle {
        return CappedLanePlanCycle(
            code = code,
            title = title,
            stopCount = stopCount,
            attributes = attributes,
            reviewer = reviewer,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped lane plan cycle in status requested. */
fun newCappedLanePlanCycle(code: String, title: String): CappedLanePlanCycle {
    return CappedLanePlanCycle(
        code = code,
        title = title,
        stopCount = 0,
        attributes = mutableMapOf<String, String>(),
        reviewer = "",
        distanceKm = 0.0,
        status = CappedLanePlanCycleStatus.REQUESTED,
    )
}
