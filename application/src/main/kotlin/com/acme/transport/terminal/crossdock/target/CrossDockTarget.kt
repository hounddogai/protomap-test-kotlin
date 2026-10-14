package com.acme.transport.terminal.crossdock.target

import com.acme.transport.kit.text.normalize

// The cross dock target records of terminal's cross dock work, and the rules, stores, and calculations around
// them.

/** Where a cross dock target is in its life cycle. */
enum class CrossDockTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a cross dock target and its current status. */
data class CrossDockTarget(
    var code: String,
    var name: String,
    var depot: String,
    var region: String,
    var pinned: Boolean,
    var label: String,
    var currency: String,
    var maximum: Double,
    var title: String,
    var status: CrossDockTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockTargetStatus): Boolean {
        when (status) {
            CrossDockTargetStatus.CREATED -> return next == CrossDockTargetStatus.VALIDATED
            CrossDockTargetStatus.VALIDATED -> return next == CrossDockTargetStatus.PUBLISHED
            CrossDockTargetStatus.PUBLISHED -> {
                when (next) {
                    CrossDockTargetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockTargetStatus) {
        if (!canMoveTo(next)) {
            error("cross dock target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"cross dock target"} $code"

    fun measuredValue(): Double = maximum

    fun snapshot(): CrossDockTarget {
        return CrossDockTarget(
            code = code,
            name = name,
            depot = depot,
            region = region,
            pinned = pinned,
            label = label,
            currency = currency,
            maximum = maximum,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a cross dock target in status created. */
fun newCrossDockTarget(code: String, name: String): CrossDockTarget {
    return CrossDockTarget(
        code = code,
        name = name,
        depot = "",
        region = "",
        pinned = false,
        label = "",
        currency = "",
        maximum = 0.0,
        title = "",
        status = CrossDockTargetStatus.CREATED,
    )
}
