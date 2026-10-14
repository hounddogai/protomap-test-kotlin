package com.acme.transport.lastmile.parcel.tieredparcelvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional parcel cycle is in its life cycle. */
enum class RegionalParcelCycleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One regional parcel cycle, with what the planners need to act on it. */
data class RegionalParcelCycle(
    var code: String,
    var name: String,
    var dock: String,
    var capacity: Int,
    var active: Boolean,
    var distanceKm: Double,
    var status: RegionalParcelCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalParcelCycleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalParcelCycleStatus): Boolean {
        when (status) {
            RegionalParcelCycleStatus.OPEN -> return next == RegionalParcelCycleStatus.REOPENED || next == RegionalParcelCycleStatus.ARCHIVED
            RegionalParcelCycleStatus.IN_REVIEW -> {
                when (next) {
                    RegionalParcelCycleStatus.REOPENED, RegionalParcelCycleStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalParcelCycleStatus.RESOLVED -> {
                when (next) {
                    RegionalParcelCycleStatus.REOPENED, RegionalParcelCycleStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalParcelCycleStatus.REOPENED -> return next == RegionalParcelCycleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RegionalParcelCycleStatus) {
        if (!canMoveTo(next)) {
            error("regional parcel cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round((capacity.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): RegionalParcelCycle {
        return RegionalParcelCycle(
            code = code,
            name = name,
            dock = dock,
            capacity = capacity,
            active = active,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional parcel cycle in status open. */
fun newRegionalParcelCycle(code: String, name: String): RegionalParcelCycle {
    return RegionalParcelCycle(
        code = code,
        name = name,
        dock = "",
        capacity = 0,
        active = false,
        distanceKm = 0.0,
        status = RegionalParcelCycleStatus.OPEN,
    )
}
