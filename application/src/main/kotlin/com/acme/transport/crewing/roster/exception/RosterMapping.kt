package com.acme.transport.crewing.roster.exception

import com.acme.transport.kit.text.normalize

/** Where a roster mapping is in its life cycle. */
enum class RosterMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One roster mapping, with what the planners need to act on it. */
data class RosterMapping(
    var code: String,
    var label: String,
    var routeCode: String,
    var externalRef: String,
    var pallets: Int,
    var status: RosterMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterMappingStatus): Boolean {
        when (status) {
            RosterMappingStatus.REQUESTED -> {
                when (next) {
                    RosterMappingStatus.GRANTED -> return true
                }
                return false
            }
            RosterMappingStatus.GRANTED -> {
                when (next) {
                    RosterMappingStatus.REVOKED -> return true
                }
                return false
            }
            RosterMappingStatus.REVOKED -> return next == RosterMappingStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RosterMappingStatus) {
        if (!canMoveTo(next)) {
            error("roster mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): RosterMapping {
        return RosterMapping(
            code = code,
            label = label,
            routeCode = routeCode,
            externalRef = externalRef,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a roster mapping in status requested. */
fun newRosterMapping(code: String, label: String): RosterMapping {
    return RosterMapping(
        code = code,
        label = label,
        routeCode = "",
        externalRef = "",
        pallets = 0,
        status = RosterMappingStatus.REQUESTED,
    )
}
