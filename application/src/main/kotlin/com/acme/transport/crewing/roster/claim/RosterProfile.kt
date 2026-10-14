package com.acme.transport.crewing.roster.claim

import com.acme.transport.kit.text.normalize

/** Where a roster profile is in its life cycle. */
enum class RosterProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One roster profile, with what the planners need to act on it. */
data class RosterProfile(
    var code: String,
    var label: String,
    var category: String,
    var visible: Boolean,
    var sequence: Int,
    var status: RosterProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterProfileStatus): Boolean {
        when (status) {
            RosterProfileStatus.CREATED -> {
                when (next) {
                    RosterProfileStatus.VALIDATED, RosterProfileStatus.PUBLISHED -> return true
                }
                return false
            }
            RosterProfileStatus.VALIDATED -> return next == RosterProfileStatus.PUBLISHED || next == RosterProfileStatus.RETIRED
            RosterProfileStatus.PUBLISHED -> return next == RosterProfileStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RosterProfileStatus) {
        if (!canMoveTo(next)) {
            error("roster profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"roster profile"} $code"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): RosterProfile {
        return RosterProfile(
            code = code,
            label = label,
            category = category,
            visible = visible,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a roster profile in status created. */
fun newRosterProfile(code: String, label: String): RosterProfile {
    return RosterProfile(
        code = code,
        label = label,
        category = "",
        visible = false,
        sequence = 0,
        status = RosterProfileStatus.CREATED,
    )
}
