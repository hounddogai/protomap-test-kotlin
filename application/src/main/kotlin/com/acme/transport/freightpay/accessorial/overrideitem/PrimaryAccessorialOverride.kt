package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary accessorial override records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a primary accessorial override is in its life cycle. */
enum class PrimaryAccessorialOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary accessorial override as the accessorial team keeps it. */
data class PrimaryAccessorialOverride(
    var code: String,
    var name: String,
    var limit: Int,
    var discount: Double,
    var reviewer: String,
    var timezone: String,
    var status: PrimaryAccessorialOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAccessorialOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAccessorialOverrideStatus): Boolean {
        when (status) {
            PrimaryAccessorialOverrideStatus.PROPOSED -> return next == PrimaryAccessorialOverrideStatus.REJECTED || next == PrimaryAccessorialOverrideStatus.WITHDRAWN
            PrimaryAccessorialOverrideStatus.APPROVED -> {
                when (next) {
                    PrimaryAccessorialOverrideStatus.REJECTED, PrimaryAccessorialOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryAccessorialOverrideStatus.REJECTED -> {
                when (next) {
                    PrimaryAccessorialOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryAccessorialOverrideStatus) {
        if (!canMoveTo(next)) {
            error("primary accessorial override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveTotal(): Double = round((limit.toDouble() - discount) * 100) / 100

    fun snapshot(): PrimaryAccessorialOverride {
        return PrimaryAccessorialOverride(
            code = code,
            name = name,
            limit = limit,
            discount = discount,
            reviewer = reviewer,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary accessorial override in status proposed. */
fun newPrimaryAccessorialOverride(code: String, name: String): PrimaryAccessorialOverride {
    return PrimaryAccessorialOverride(
        code = code,
        name = name,
        limit = 0,
        discount = 0.0,
        reviewer = "",
        timezone = "",
        status = PrimaryAccessorialOverrideStatus.PROPOSED,
    )
}
