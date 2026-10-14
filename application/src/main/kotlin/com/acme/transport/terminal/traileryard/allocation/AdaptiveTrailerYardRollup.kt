package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive trailer yard rollup is in its life cycle. */
enum class AdaptiveTrailerYardRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a adaptive trailer yard rollup and its current status. */
data class AdaptiveTrailerYardRollup(
    var code: String,
    var label: String,
    var unit: String,
    var quantity: Int,
    var score: Double,
    var description: String,
    var floorValue: Double,
    var margin: Double,
    var priority: Int,
    var capacity: Int,
    var driverId: String,
    var status: AdaptiveTrailerYardRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTrailerYardRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTrailerYardRollupStatus): Boolean {
        when (status) {
            AdaptiveTrailerYardRollupStatus.OPEN -> return next == AdaptiveTrailerYardRollupStatus.IN_REVIEW
            AdaptiveTrailerYardRollupStatus.IN_REVIEW -> return next == AdaptiveTrailerYardRollupStatus.REOPENED || next == AdaptiveTrailerYardRollupStatus.ARCHIVED
            AdaptiveTrailerYardRollupStatus.RESOLVED -> return next == AdaptiveTrailerYardRollupStatus.REOPENED || next == AdaptiveTrailerYardRollupStatus.ARCHIVED
            AdaptiveTrailerYardRollupStatus.REOPENED -> return next == AdaptiveTrailerYardRollupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AdaptiveTrailerYardRollupStatus) {
        if (!canMoveTo(next)) {
            error("adaptive trailer yard rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive trailer yard rollup"} $code"

    fun weightedLoad(): Double = round((score - floorValue) * 100) / 100

    fun snapshot(): AdaptiveTrailerYardRollup {
        return AdaptiveTrailerYardRollup(
            code = code,
            label = label,
            unit = unit,
            quantity = quantity,
            score = score,
            description = description,
            floorValue = floorValue,
            margin = margin,
            priority = priority,
            capacity = capacity,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive trailer yard rollup in status open. */
fun newAdaptiveTrailerYardRollup(code: String, label: String): AdaptiveTrailerYardRollup {
    return AdaptiveTrailerYardRollup(
        code = code,
        label = label,
        unit = "",
        quantity = 0,
        score = 0.0,
        description = "",
        floorValue = 0.0,
        margin = 0.0,
        priority = 0,
        capacity = 0,
        driverId = "",
        status = AdaptiveTrailerYardRollupStatus.OPEN,
    )
}
