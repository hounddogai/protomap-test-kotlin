package com.acme.transport.customs.inspection.entry

import com.acme.transport.kit.text.normalize

/** Where a global inspection schedule is in its life cycle. */
enum class GlobalInspectionScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One global inspection schedule, with what the planners need to act on it. */
data class GlobalInspectionSchedule(
    var code: String,
    var name: String,
    var notes: String,
    var currency: String,
    var attributes: MutableMap<String, String>,
    var depot: String,
    var loadKg: Double,
    var channel: String,
    var status: GlobalInspectionScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalInspectionScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalInspectionScheduleStatus): Boolean {
        when (status) {
            GlobalInspectionScheduleStatus.CREATED -> {
                when (next) {
                    GlobalInspectionScheduleStatus.VALIDATED, GlobalInspectionScheduleStatus.RETIRED -> return true
                }
                return false
            }
            GlobalInspectionScheduleStatus.VALIDATED -> {
                when (next) {
                    GlobalInspectionScheduleStatus.PUBLISHED -> return true
                }
                return false
            }
            GlobalInspectionScheduleStatus.PUBLISHED -> {
                when (next) {
                    GlobalInspectionScheduleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalInspectionScheduleStatus) {
        if (!canMoveTo(next)) {
            error("global inspection schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global inspection schedule"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = loadKg

    fun snapshot(): GlobalInspectionSchedule {
        return GlobalInspectionSchedule(
            code = code,
            name = name,
            notes = notes,
            currency = currency,
            attributes = attributes,
            depot = depot,
            loadKg = loadKg,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global inspection schedule in status created. */
fun newGlobalInspectionSchedule(code: String, name: String): GlobalInspectionSchedule {
    return GlobalInspectionSchedule(
        code = code,
        name = name,
        notes = "",
        currency = "",
        attributes = mutableMapOf<String, String>(),
        depot = "",
        loadKg = 0.0,
        channel = "",
        status = GlobalInspectionScheduleStatus.CREATED,
    )
}
