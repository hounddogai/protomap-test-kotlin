package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual trailer yard adjustment is in its life cycle. */
enum class ManualTrailerYardAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a manual trailer yard adjustment and its current status. */
data class ManualTrailerYardAdjustment(
    var code: String,
    var name: String,
    var startsAt: Int,
    var trailerId: String,
    var revision: Int,
    var status: ManualTrailerYardAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTrailerYardAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTrailerYardAdjustmentStatus): Boolean {
        when (status) {
            ManualTrailerYardAdjustmentStatus.PROPOSED -> return next == ManualTrailerYardAdjustmentStatus.REJECTED
            ManualTrailerYardAdjustmentStatus.APPROVED -> return next == ManualTrailerYardAdjustmentStatus.REJECTED || next == ManualTrailerYardAdjustmentStatus.WITHDRAWN
            ManualTrailerYardAdjustmentStatus.REJECTED -> return next == ManualTrailerYardAdjustmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualTrailerYardAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("manual trailer yard adjustment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun projectedTotal(): Double = round((startsAt.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): ManualTrailerYardAdjustment {
        return ManualTrailerYardAdjustment(
            code = code,
            name = name,
            startsAt = startsAt,
            trailerId = trailerId,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual trailer yard adjustment in status proposed. */
fun newManualTrailerYardAdjustment(code: String, name: String): ManualTrailerYardAdjustment {
    return ManualTrailerYardAdjustment(
        code = code,
        name = name,
        startsAt = 0,
        trailerId = "",
        revision = 0,
        status = ManualTrailerYardAdjustmentStatus.PROPOSED,
    )
}
