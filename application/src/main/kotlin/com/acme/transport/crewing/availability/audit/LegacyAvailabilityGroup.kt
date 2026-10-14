package com.acme.transport.crewing.availability.audit

import com.acme.transport.kit.text.normalize

/** Where a legacy availability group is in its life cycle. */
enum class LegacyAvailabilityGroupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy availability group as the availability team keeps it. */
data class LegacyAvailabilityGroup(
    var code: String,
    var label: String,
    var country: String,
    var pinned: Boolean,
    var currency: String,
    var quantity: Int,
    var status: LegacyAvailabilityGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAvailabilityGroupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAvailabilityGroupStatus): Boolean {
        when (status) {
            LegacyAvailabilityGroupStatus.PROPOSED -> return next == LegacyAvailabilityGroupStatus.WITHDRAWN
            LegacyAvailabilityGroupStatus.APPROVED -> return next == LegacyAvailabilityGroupStatus.REJECTED
            LegacyAvailabilityGroupStatus.REJECTED -> {
                when (next) {
                    LegacyAvailabilityGroupStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyAvailabilityGroupStatus) {
        if (!canMoveTo(next)) {
            error("legacy availability group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy availability group"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): LegacyAvailabilityGroup {
        return LegacyAvailabilityGroup(
            code = code,
            label = label,
            country = country,
            pinned = pinned,
            currency = currency,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy availability group in status proposed. */
fun newLegacyAvailabilityGroup(code: String, label: String): LegacyAvailabilityGroup {
    return LegacyAvailabilityGroup(
        code = code,
        label = label,
        country = "",
        pinned = false,
        currency = "",
        quantity = 0,
        status = LegacyAvailabilityGroupStatus.PROPOSED,
    )
}
