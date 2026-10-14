package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.text.normalize

/** Where a roster group is in its life cycle. */
enum class RosterGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a roster group and its current status. */
data class RosterGroup(
    var code: String,
    var title: String,
    var summary: String,
    var endsAt: Int,
    var currency: String,
    var status: RosterGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterGroupStatus): Boolean {
        when (status) {
            RosterGroupStatus.REQUESTED -> {
                when (next) {
                    RosterGroupStatus.GRANTED, RosterGroupStatus.EXPIRED -> return true
                }
                return false
            }
            RosterGroupStatus.GRANTED -> return next == RosterGroupStatus.REVOKED || next == RosterGroupStatus.EXPIRED
            RosterGroupStatus.REVOKED -> {
                when (next) {
                    RosterGroupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterGroupStatus) {
        if (!canMoveTo(next)) {
            error("roster group $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"roster group"} $code"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): RosterGroup {
        return RosterGroup(
            code = code,
            title = title,
            summary = summary,
            endsAt = endsAt,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a roster group in status requested. */
fun newRosterGroup(code: String, title: String): RosterGroup {
    return RosterGroup(
        code = code,
        title = title,
        summary = "",
        endsAt = 0,
        currency = "",
        status = RosterGroupStatus.REQUESTED,
    )
}
