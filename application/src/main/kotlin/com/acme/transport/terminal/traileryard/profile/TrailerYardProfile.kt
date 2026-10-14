package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer yard profile records of terminal's trailer yard work, and the rules, stores, and calculations
// around them.

/** Where a trailer yard profile is in its life cycle. */
enum class TrailerYardProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A trailer yard profile as the trailer yard team keeps it. */
data class TrailerYardProfile(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var rate: Double,
    var stopCount: Int,
    var status: TrailerYardProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardProfileStatus): Boolean {
        when (status) {
            TrailerYardProfileStatus.CREATED -> return next == TrailerYardProfileStatus.VALIDATED || next == TrailerYardProfileStatus.RETIRED
            TrailerYardProfileStatus.VALIDATED -> return next == TrailerYardProfileStatus.PUBLISHED
            TrailerYardProfileStatus.PUBLISHED -> return next == TrailerYardProfileStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TrailerYardProfileStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard profile $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"trailer yard profile"} $code"

    fun adjustedTotal(): Double = round((stopCount.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardProfile {
        return TrailerYardProfile(
            code = code,
            label = label,
            updatedAt = updatedAt,
            rate = rate,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer yard profile in status created. */
fun newTrailerYardProfile(code: String, label: String): TrailerYardProfile {
    return TrailerYardProfile(
        code = code,
        label = label,
        updatedAt = 0,
        rate = 0.0,
        stopCount = 0,
        status = TrailerYardProfileStatus.CREATED,
    )
}
