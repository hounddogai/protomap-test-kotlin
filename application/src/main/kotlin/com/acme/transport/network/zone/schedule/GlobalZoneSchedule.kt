package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.text.normalize

// The global zone schedule records of network's zone work, and the rules, stores, and calculations around
// them.

/** Where a global zone schedule is in its life cycle. */
enum class GlobalZoneScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A global zone schedule as the zone team keeps it. */
data class GlobalZoneSchedule(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var weight: Double,
    var category: String,
    var status: GlobalZoneScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalZoneScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalZoneScheduleStatus): Boolean {
        when (status) {
            GlobalZoneScheduleStatus.CREATED -> {
                when (next) {
                    GlobalZoneScheduleStatus.VALIDATED -> return true
                }
                return false
            }
            GlobalZoneScheduleStatus.VALIDATED -> return next == GlobalZoneScheduleStatus.PUBLISHED || next == GlobalZoneScheduleStatus.RETIRED
            GlobalZoneScheduleStatus.PUBLISHED -> return next == GlobalZoneScheduleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: GlobalZoneScheduleStatus) {
        if (!canMoveTo(next)) {
            error("global zone schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global zone schedule"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = weight

    fun snapshot(): GlobalZoneSchedule {
        return GlobalZoneSchedule(
            code = code,
            title = title,
            attributes = attributes,
            weight = weight,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a global zone schedule in status created. */
fun newGlobalZoneSchedule(code: String, title: String): GlobalZoneSchedule {
    return GlobalZoneSchedule(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        weight = 0.0,
        category = "",
        status = GlobalZoneScheduleStatus.CREATED,
    )
}
