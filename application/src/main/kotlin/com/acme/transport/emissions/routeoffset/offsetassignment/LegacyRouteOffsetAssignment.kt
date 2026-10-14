package com.acme.transport.emissions.routeoffset.offsetassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy route offset assignment records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a legacy route offset assignment is in its life cycle. */
enum class LegacyRouteOffsetAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy route offset assignment. Its code is unique within a tenant. */
data class LegacyRouteOffsetAssignment(
    var code: String,
    var label: String,
    var capacity: Int,
    var startsAt: Int,
    var country: String,
    var dock: String,
    var odometer: Int,
    var rate: Double,
    var status: LegacyRouteOffsetAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRouteOffsetAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRouteOffsetAssignmentStatus): Boolean {
        when (status) {
            LegacyRouteOffsetAssignmentStatus.NEW -> return next == LegacyRouteOffsetAssignmentStatus.RUNNING || next == LegacyRouteOffsetAssignmentStatus.DONE
            LegacyRouteOffsetAssignmentStatus.QUEUED -> return next == LegacyRouteOffsetAssignmentStatus.RUNNING
            LegacyRouteOffsetAssignmentStatus.RUNNING -> {
                when (next) {
                    LegacyRouteOffsetAssignmentStatus.FAILED -> return true
                }
                return false
            }
            LegacyRouteOffsetAssignmentStatus.DONE -> return next == LegacyRouteOffsetAssignmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyRouteOffsetAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("legacy route offset assignment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedLoad(): Double = round((capacity.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): LegacyRouteOffsetAssignment {
        return LegacyRouteOffsetAssignment(
            code = code,
            label = label,
            capacity = capacity,
            startsAt = startsAt,
            country = country,
            dock = dock,
            odometer = odometer,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy route offset assignment in status new. */
fun newLegacyRouteOffsetAssignment(code: String, label: String): LegacyRouteOffsetAssignment {
    return LegacyRouteOffsetAssignment(
        code = code,
        label = label,
        capacity = 0,
        startsAt = 0,
        country = "",
        dock = "",
        odometer = 0,
        rate = 0.0,
        status = LegacyRouteOffsetAssignmentStatus.NEW,
    )
}
