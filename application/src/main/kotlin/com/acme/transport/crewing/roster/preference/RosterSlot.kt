package com.acme.transport.crewing.roster.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster slot is in its life cycle. */
enum class RosterSlotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A roster slot. Its code is unique within a tenant. */
data class RosterSlot(
    var code: String,
    var label: String,
    var threshold: Double,
    var active: Boolean,
    var category: String,
    var distanceKm: Double,
    var reviewer: String,
    var notes: String,
    var phone: String,
    var routeCode: String,
    var status: RosterSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterSlotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterSlotStatus): Boolean {
        when (status) {
            RosterSlotStatus.CREATED -> {
                when (next) {
                    RosterSlotStatus.VALIDATED -> return true
                }
                return false
            }
            RosterSlotStatus.VALIDATED -> return next == RosterSlotStatus.PUBLISHED || next == RosterSlotStatus.RETIRED
            RosterSlotStatus.PUBLISHED -> return next == RosterSlotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RosterSlotStatus) {
        if (!canMoveTo(next)) {
            error("roster slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"roster slot"} $code"

    fun effectiveExposure(): Double = round((threshold - distanceKm) * 100) / 100

    fun snapshot(): RosterSlot {
        return RosterSlot(
            code = code,
            label = label,
            threshold = threshold,
            active = active,
            category = category,
            distanceKm = distanceKm,
            reviewer = reviewer,
            notes = notes,
            phone = phone,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a roster slot in status created. */
fun newRosterSlot(code: String, label: String): RosterSlot {
    return RosterSlot(
        code = code,
        label = label,
        threshold = 0.0,
        active = false,
        category = "",
        distanceKm = 0.0,
        reviewer = "",
        notes = "",
        phone = "",
        routeCode = "",
        status = RosterSlotStatus.CREATED,
    )
}
