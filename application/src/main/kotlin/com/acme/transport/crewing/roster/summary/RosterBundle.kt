package com.acme.transport.crewing.roster.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster bundle is in its life cycle. */
enum class RosterBundleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A roster bundle. Its code is unique within a tenant. */
data class RosterBundle(
    var code: String,
    var name: String,
    var externalRef: String,
    var odometer: Int,
    var priority: Int,
    var unit: String,
    var status: RosterBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterBundleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterBundleStatus): Boolean {
        when (status) {
            RosterBundleStatus.CREATED -> return next == RosterBundleStatus.RETIRED
            RosterBundleStatus.VALIDATED -> {
                when (next) {
                    RosterBundleStatus.RETIRED -> return true
                }
                return false
            }
            RosterBundleStatus.PUBLISHED -> {
                when (next) {
                    RosterBundleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterBundleStatus) {
        if (!canMoveTo(next)) {
            error("roster bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netValue(): Double = round(odometer.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): RosterBundle {
        return RosterBundle(
            code = code,
            name = name,
            externalRef = externalRef,
            odometer = odometer,
            priority = priority,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a roster bundle in status created. */
fun newRosterBundle(code: String, name: String): RosterBundle {
    return RosterBundle(
        code = code,
        name = name,
        externalRef = "",
        odometer = 0,
        priority = 0,
        unit = "",
        status = RosterBundleStatus.CREATED,
    )
}
