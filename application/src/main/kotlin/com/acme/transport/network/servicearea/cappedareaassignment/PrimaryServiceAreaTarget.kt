package com.acme.transport.network.servicearea.cappedareaassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary service area target is in its life cycle. */
enum class PrimaryServiceAreaTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One primary service area target, with what the planners need to act on it. */
data class PrimaryServiceAreaTarget(
    var code: String,
    var title: String,
    var trailerId: String,
    var approved: Boolean,
    var margin: Double,
    var threshold: Double,
    var unit: String,
    var externalRef: String,
    var summary: String,
    var cost: Double,
    var status: PrimaryServiceAreaTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryServiceAreaTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryServiceAreaTargetStatus): Boolean {
        when (status) {
            PrimaryServiceAreaTargetStatus.PROPOSED -> {
                when (next) {
                    PrimaryServiceAreaTargetStatus.REJECTED -> return true
                }
                return false
            }
            PrimaryServiceAreaTargetStatus.APPROVED -> return next == PrimaryServiceAreaTargetStatus.REJECTED
            PrimaryServiceAreaTargetStatus.REJECTED -> {
                when (next) {
                    PrimaryServiceAreaTargetStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryServiceAreaTargetStatus) {
        if (!canMoveTo(next)) {
            error("primary service area target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netTotal(): Double = round((threshold - margin) * 100) / 100

    fun snapshot(): PrimaryServiceAreaTarget {
        return PrimaryServiceAreaTarget(
            code = code,
            title = title,
            trailerId = trailerId,
            approved = approved,
            margin = margin,
            threshold = threshold,
            unit = unit,
            externalRef = externalRef,
            summary = summary,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a primary service area target in status proposed. */
fun newPrimaryServiceAreaTarget(code: String, title: String): PrimaryServiceAreaTarget {
    return PrimaryServiceAreaTarget(
        code = code,
        title = title,
        trailerId = "",
        approved = false,
        margin = 0.0,
        threshold = 0.0,
        unit = "",
        externalRef = "",
        summary = "",
        cost = 0.0,
        status = PrimaryServiceAreaTargetStatus.PROPOSED,
    )
}
