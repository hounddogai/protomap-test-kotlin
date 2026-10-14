package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.text.normalize

/** Where a strict clearance group is in its life cycle. */
enum class StrictClearanceGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a strict clearance group and its current status. */
data class StrictClearanceGroup(
    var code: String,
    var label: String,
    var carrier: String,
    var etaMinutes: Int,
    var name: String,
    var status: StrictClearanceGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictClearanceGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictClearanceGroupStatus): Boolean {
        when (status) {
            StrictClearanceGroupStatus.REQUESTED -> {
                when (next) {
                    StrictClearanceGroupStatus.GRANTED, StrictClearanceGroupStatus.EXPIRED -> return true
                }
                return false
            }
            StrictClearanceGroupStatus.GRANTED -> {
                when (next) {
                    StrictClearanceGroupStatus.EXPIRED -> return true
                }
                return false
            }
            StrictClearanceGroupStatus.REVOKED -> {
                when (next) {
                    StrictClearanceGroupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictClearanceGroupStatus) {
        if (!canMoveTo(next)) {
            error("strict clearance group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): StrictClearanceGroup {
        return StrictClearanceGroup(
            code = code,
            label = label,
            carrier = carrier,
            etaMinutes = etaMinutes,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict clearance group in status requested. */
fun newStrictClearanceGroup(code: String, label: String): StrictClearanceGroup {
    return StrictClearanceGroup(
        code = code,
        label = label,
        carrier = "",
        etaMinutes = 0,
        name = "",
        status = StrictClearanceGroupStatus.REQUESTED,
    )
}
