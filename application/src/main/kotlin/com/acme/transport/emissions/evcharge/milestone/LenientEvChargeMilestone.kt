package com.acme.transport.emissions.evcharge.milestone

import com.acme.transport.kit.text.normalize

// The lenient ev charge milestone records of emissions's ev charge work, and the rules, stores, and
// calculations around them.

/** Where a lenient ev charge milestone is in its life cycle. */
enum class LenientEvChargeMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lenient ev charge milestone. Its code is unique within a tenant. */
data class LenientEvChargeMilestone(
    var code: String,
    var name: String,
    var odometer: Int,
    var active: Boolean,
    var ownerId: String,
    var description: String,
    var shiftCode: String,
    var externalRef: String,
    var status: LenientEvChargeMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientEvChargeMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientEvChargeMilestoneStatus): Boolean {
        when (status) {
            LenientEvChargeMilestoneStatus.OPEN -> {
                when (next) {
                    LenientEvChargeMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientEvChargeMilestoneStatus.IN_REVIEW -> {
                when (next) {
                    LenientEvChargeMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientEvChargeMilestoneStatus.RESOLVED -> return next == LenientEvChargeMilestoneStatus.REOPENED || next == LenientEvChargeMilestoneStatus.ARCHIVED
            LenientEvChargeMilestoneStatus.REOPENED -> {
                when (next) {
                    LenientEvChargeMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientEvChargeMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("lenient ev charge milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): LenientEvChargeMilestone {
        return LenientEvChargeMilestone(
            code = code,
            name = name,
            odometer = odometer,
            active = active,
            ownerId = ownerId,
            description = description,
            shiftCode = shiftCode,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient ev charge milestone in status open. */
fun newLenientEvChargeMilestone(code: String, name: String): LenientEvChargeMilestone {
    return LenientEvChargeMilestone(
        code = code,
        name = name,
        odometer = 0,
        active = false,
        ownerId = "",
        description = "",
        shiftCode = "",
        externalRef = "",
        status = LenientEvChargeMilestoneStatus.OPEN,
    )
}
