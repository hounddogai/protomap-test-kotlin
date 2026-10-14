package com.acme.transport.crewing.swap.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global swap override is in its life cycle. */
enum class GlobalSwapOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A global swap override as the swap team keeps it. */
data class GlobalSwapOverride(
    var code: String,
    var label: String,
    var approved: Boolean,
    var retries: Int,
    var cost: Double,
    var vehicleId: String,
    var depot: String,
    var status: GlobalSwapOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalSwapOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalSwapOverrideStatus): Boolean {
        when (status) {
            GlobalSwapOverrideStatus.CREATED -> return next == GlobalSwapOverrideStatus.VALIDATED || next == GlobalSwapOverrideStatus.PUBLISHED
            GlobalSwapOverrideStatus.VALIDATED -> return next == GlobalSwapOverrideStatus.PUBLISHED || next == GlobalSwapOverrideStatus.RETIRED
            GlobalSwapOverrideStatus.PUBLISHED -> {
                when (next) {
                    GlobalSwapOverrideStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalSwapOverrideStatus) {
        if (!canMoveTo(next)) {
            error("global swap override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedTotal(): Double = round(retries.toDouble() * cost * 100) / 100

    fun snapshot(): GlobalSwapOverride {
        return GlobalSwapOverride(
            code = code,
            label = label,
            approved = approved,
            retries = retries,
            cost = cost,
            vehicleId = vehicleId,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global swap override in status created. */
fun newGlobalSwapOverride(code: String, label: String): GlobalSwapOverride {
    return GlobalSwapOverride(
        code = code,
        label = label,
        approved = false,
        retries = 0,
        cost = 0.0,
        vehicleId = "",
        depot = "",
        status = GlobalSwapOverrideStatus.CREATED,
    )
}
