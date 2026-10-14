package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.text.normalize

// The manual backhaul override records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a manual backhaul override is in its life cycle. */
enum class ManualBackhaulOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One manual backhaul override, with what the planners need to act on it. */
data class ManualBackhaulOverride(
    var code: String,
    var title: String,
    var capacity: Int,
    var timezone: String,
    var summary: String,
    var status: ManualBackhaulOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualBackhaulOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualBackhaulOverrideStatus): Boolean {
        when (status) {
            ManualBackhaulOverrideStatus.CREATED -> return next == ManualBackhaulOverrideStatus.PUBLISHED
            ManualBackhaulOverrideStatus.VALIDATED -> {
                when (next) {
                    ManualBackhaulOverrideStatus.PUBLISHED -> return true
                }
                return false
            }
            ManualBackhaulOverrideStatus.PUBLISHED -> return next == ManualBackhaulOverrideStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualBackhaulOverrideStatus) {
        if (!canMoveTo(next)) {
            error("manual backhaul override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): ManualBackhaulOverride {
        return ManualBackhaulOverride(
            code = code,
            title = title,
            capacity = capacity,
            timezone = timezone,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual backhaul override in status created. */
fun newManualBackhaulOverride(code: String, title: String): ManualBackhaulOverride {
    return ManualBackhaulOverride(
        code = code,
        title = title,
        capacity = 0,
        timezone = "",
        summary = "",
        status = ManualBackhaulOverrideStatus.CREATED,
    )
}
