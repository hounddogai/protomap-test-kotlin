package com.acme.transport.customs.hold.policy

import com.acme.transport.kit.text.normalize

/** Where a hold override is in its life cycle. */
enum class HoldOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A hold override as the hold team keeps it. */
data class HoldOverride(
    var code: String,
    var title: String,
    var depot: String,
    var contactEmail: String,
    var source: String,
    var quantity: Int,
    var status: HoldOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldOverrideStatus): Boolean {
        when (status) {
            HoldOverrideStatus.CREATED -> return next == HoldOverrideStatus.VALIDATED || next == HoldOverrideStatus.PUBLISHED
            HoldOverrideStatus.VALIDATED -> {
                when (next) {
                    HoldOverrideStatus.PUBLISHED -> return true
                }
                return false
            }
            HoldOverrideStatus.PUBLISHED -> return next == HoldOverrideStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: HoldOverrideStatus) {
        if (!canMoveTo(next)) {
            error("hold override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"hold override"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): HoldOverride {
        return HoldOverride(
            code = code,
            title = title,
            depot = depot,
            contactEmail = contactEmail,
            source = source,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a hold override in status created. */
fun newHoldOverride(code: String, title: String): HoldOverride {
    return HoldOverride(
        code = code,
        title = title,
        depot = "",
        contactEmail = "",
        source = "",
        quantity = 0,
        status = HoldOverrideStatus.CREATED,
    )
}
