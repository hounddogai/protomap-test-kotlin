package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rest break adjustment records of crewing's rest break work, and the rules, stores, and calculations
// around them.

/** Where a rest break adjustment is in its life cycle. */
enum class RestBreakAdjustmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a rest break adjustment and its current status. */
data class RestBreakAdjustment(
    var code: String,
    var label: String,
    var title: String,
    var pallets: Int,
    var ratio: Double,
    var weight: Double,
    var priority: Int,
    var reviewer: String,
    var status: RestBreakAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakAdjustmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakAdjustmentStatus): Boolean {
        when (status) {
            RestBreakAdjustmentStatus.OPEN -> {
                when (next) {
                    RestBreakAdjustmentStatus.IN_REVIEW, RestBreakAdjustmentStatus.RESOLVED -> return true
                }
                return false
            }
            RestBreakAdjustmentStatus.IN_REVIEW -> return next == RestBreakAdjustmentStatus.RESOLVED || next == RestBreakAdjustmentStatus.ARCHIVED
            RestBreakAdjustmentStatus.RESOLVED -> return next == RestBreakAdjustmentStatus.REOPENED || next == RestBreakAdjustmentStatus.ARCHIVED
            RestBreakAdjustmentStatus.REOPENED -> {
                when (next) {
                    RestBreakAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("rest break adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedExposure(): Double = round(pallets.toDouble() * ratio * 100) / 100

    fun snapshot(): RestBreakAdjustment {
        return RestBreakAdjustment(
            code = code,
            label = label,
            title = title,
            pallets = pallets,
            ratio = ratio,
            weight = weight,
            priority = priority,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rest break adjustment in status open. */
fun newRestBreakAdjustment(code: String, label: String): RestBreakAdjustment {
    return RestBreakAdjustment(
        code = code,
        label = label,
        title = "",
        pallets = 0,
        ratio = 0.0,
        weight = 0.0,
        priority = 0,
        reviewer = "",
        status = RestBreakAdjustmentStatus.OPEN,
    )
}
