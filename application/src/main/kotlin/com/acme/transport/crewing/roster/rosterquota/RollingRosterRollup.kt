package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.text.normalize

/** Where a rolling roster rollup is in its life cycle. */
enum class RollingRosterRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One rolling roster rollup, with what the planners need to act on it. */
data class RollingRosterRollup(
    var code: String,
    var label: String,
    var country: String,
    var carrier: String,
    var externalRef: String,
    var stopCount: Int,
    var summary: String,
    var unit: String,
    var routeCode: String,
    var description: String,
    var status: RollingRosterRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRosterRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRosterRollupStatus): Boolean {
        when (status) {
            RollingRosterRollupStatus.CREATED -> return next == RollingRosterRollupStatus.PUBLISHED
            RollingRosterRollupStatus.VALIDATED -> {
                when (next) {
                    RollingRosterRollupStatus.PUBLISHED, RollingRosterRollupStatus.RETIRED -> return true
                }
                return false
            }
            RollingRosterRollupStatus.PUBLISHED -> {
                when (next) {
                    RollingRosterRollupStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingRosterRollupStatus) {
        if (!canMoveTo(next)) {
            error("rolling roster rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling roster rollup"} $code"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): RollingRosterRollup {
        return RollingRosterRollup(
            code = code,
            label = label,
            country = country,
            carrier = carrier,
            externalRef = externalRef,
            stopCount = stopCount,
            summary = summary,
            unit = unit,
            routeCode = routeCode,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rolling roster rollup in status created. */
fun newRollingRosterRollup(code: String, label: String): RollingRosterRollup {
    return RollingRosterRollup(
        code = code,
        label = label,
        country = "",
        carrier = "",
        externalRef = "",
        stopCount = 0,
        summary = "",
        unit = "",
        routeCode = "",
        description = "",
        status = RollingRosterRollupStatus.CREATED,
    )
}
