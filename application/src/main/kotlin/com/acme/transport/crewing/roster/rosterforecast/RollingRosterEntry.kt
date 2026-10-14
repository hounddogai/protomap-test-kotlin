package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.text.normalize

/** Where a rolling roster entry is in its life cycle. */
enum class RollingRosterEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rolling roster entry. Its code is unique within a tenant. */
data class RollingRosterEntry(
    var code: String,
    var label: String,
    var driverId: String,
    var visible: Boolean,
    var externalRef: String,
    var score: Double,
    var status: RollingRosterEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRosterEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRosterEntryStatus): Boolean {
        when (status) {
            RollingRosterEntryStatus.REQUESTED -> {
                when (next) {
                    RollingRosterEntryStatus.GRANTED, RollingRosterEntryStatus.REVOKED -> return true
                }
                return false
            }
            RollingRosterEntryStatus.GRANTED -> {
                when (next) {
                    RollingRosterEntryStatus.REVOKED, RollingRosterEntryStatus.EXPIRED -> return true
                }
                return false
            }
            RollingRosterEntryStatus.REVOKED -> {
                when (next) {
                    RollingRosterEntryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingRosterEntryStatus) {
        if (!canMoveTo(next)) {
            error("rolling roster entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling roster entry"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): RollingRosterEntry {
        return RollingRosterEntry(
            code = code,
            label = label,
            driverId = driverId,
            visible = visible,
            externalRef = externalRef,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling roster entry in status requested. */
fun newRollingRosterEntry(code: String, label: String): RollingRosterEntry {
    return RollingRosterEntry(
        code = code,
        label = label,
        driverId = "",
        visible = false,
        externalRef = "",
        score = 0.0,
        status = RollingRosterEntryStatus.REQUESTED,
    )
}
