package com.acme.transport.crewing.availability.cycle

import com.acme.transport.kit.text.normalize

/** Where a availability group is in its life cycle. */
enum class AvailabilityGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a availability group and its current status. */
data class AvailabilityGroup(
    var code: String,
    var name: String,
    var budget: Double,
    var phone: String,
    var description: String,
    var status: AvailabilityGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityGroupStatus): Boolean {
        when (status) {
            AvailabilityGroupStatus.REQUESTED -> return next == AvailabilityGroupStatus.REVOKED || next == AvailabilityGroupStatus.EXPIRED
            AvailabilityGroupStatus.GRANTED -> {
                when (next) {
                    AvailabilityGroupStatus.REVOKED -> return true
                }
                return false
            }
            AvailabilityGroupStatus.REVOKED -> {
                when (next) {
                    AvailabilityGroupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityGroupStatus) {
        if (!canMoveTo(next)) {
            error("availability group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"availability group"} $code"

    fun measuredValue(): Double = budget

    fun snapshot(): AvailabilityGroup {
        return AvailabilityGroup(
            code = code,
            name = name,
            budget = budget,
            phone = phone,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a availability group in status requested. */
fun newAvailabilityGroup(code: String, name: String): AvailabilityGroup {
    return AvailabilityGroup(
        code = code,
        name = name,
        budget = 0.0,
        phone = "",
        description = "",
        status = AvailabilityGroupStatus.REQUESTED,
    )
}
