package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional trailer yard adjustment records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a regional trailer yard adjustment is in its life cycle. */
enum class RegionalTrailerYardAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One regional trailer yard adjustment, with what the planners need to act on it. */
data class RegionalTrailerYardAdjustment(
    var code: String,
    var label: String,
    var depot: String,
    var approved: Boolean,
    var description: String,
    var threshold: Double,
    var pallets: Int,
    var priority: Int,
    var ratio: Double,
    var status: RegionalTrailerYardAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTrailerYardAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTrailerYardAdjustmentStatus): Boolean {
        when (status) {
            RegionalTrailerYardAdjustmentStatus.PROPOSED -> return next == RegionalTrailerYardAdjustmentStatus.REJECTED
            RegionalTrailerYardAdjustmentStatus.APPROVED -> return next == RegionalTrailerYardAdjustmentStatus.REJECTED
            RegionalTrailerYardAdjustmentStatus.REJECTED -> {
                when (next) {
                    RegionalTrailerYardAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalTrailerYardAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("regional trailer yard adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveTotal(): Double = round((priority.toDouble() - ratio) * 100) / 100

    fun snapshot(): RegionalTrailerYardAdjustment {
        return RegionalTrailerYardAdjustment(
            code = code,
            label = label,
            depot = depot,
            approved = approved,
            description = description,
            threshold = threshold,
            pallets = pallets,
            priority = priority,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a regional trailer yard adjustment in status proposed. */
fun newRegionalTrailerYardAdjustment(code: String, label: String): RegionalTrailerYardAdjustment {
    return RegionalTrailerYardAdjustment(
        code = code,
        label = label,
        depot = "",
        approved = false,
        description = "",
        threshold = 0.0,
        pallets = 0,
        priority = 0,
        ratio = 0.0,
        status = RegionalTrailerYardAdjustmentStatus.PROPOSED,
    )
}
