package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset threshold is in its life cycle. */
enum class RouteOffsetThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a route offset threshold and its current status. */
data class RouteOffsetThreshold(
    var code: String,
    var label: String,
    var notes: String,
    var archived: Boolean,
    var description: String,
    var capacity: Int,
    var reviewer: String,
    var quantity: Int,
    var amount: Double,
    var status: RouteOffsetThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetThresholdStatus): Boolean {
        when (status) {
            RouteOffsetThresholdStatus.PROPOSED -> return next == RouteOffsetThresholdStatus.APPROVED
            RouteOffsetThresholdStatus.APPROVED -> {
                when (next) {
                    RouteOffsetThresholdStatus.REJECTED -> return true
                }
                return false
            }
            RouteOffsetThresholdStatus.REJECTED -> {
                when (next) {
                    RouteOffsetThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetThresholdStatus) {
        if (!canMoveTo(next)) {
            error("route offset threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"route offset threshold"} $code"

    fun weightedLoad(): Double = round(amount * quantity.toDouble() * 100) / 100

    fun snapshot(): RouteOffsetThreshold {
        return RouteOffsetThreshold(
            code = code,
            label = label,
            notes = notes,
            archived = archived,
            description = description,
            capacity = capacity,
            reviewer = reviewer,
            quantity = quantity,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a route offset threshold in status proposed. */
fun newRouteOffsetThreshold(code: String, label: String): RouteOffsetThreshold {
    return RouteOffsetThreshold(
        code = code,
        label = label,
        notes = "",
        archived = false,
        description = "",
        capacity = 0,
        reviewer = "",
        quantity = 0,
        amount = 0.0,
        status = RouteOffsetThresholdStatus.PROPOSED,
    )
}
