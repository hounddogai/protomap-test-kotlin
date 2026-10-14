package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard rollup is in its life cycle. */
enum class TrailerYardRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One trailer yard rollup, with what the planners need to act on it. */
data class TrailerYardRollup(
    var code: String,
    var name: String,
    var attempts: Int,
    var maximum: Double,
    var contactEmail: String,
    var timezone: String,
    var summary: String,
    var updatedAt: Int,
    var segment: String,
    var batchSize: Int,
    var status: TrailerYardRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardRollupStatus): Boolean {
        when (status) {
            TrailerYardRollupStatus.PLANNED -> {
                when (next) {
                    TrailerYardRollupStatus.CANCELLED -> return true
                }
                return false
            }
            TrailerYardRollupStatus.SCHEDULED -> return next == TrailerYardRollupStatus.IN_PROGRESS
            TrailerYardRollupStatus.IN_PROGRESS -> {
                when (next) {
                    TrailerYardRollupStatus.COMPLETED, TrailerYardRollupStatus.CANCELLED -> return true
                }
                return false
            }
            TrailerYardRollupStatus.COMPLETED -> {
                when (next) {
                    TrailerYardRollupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardRollupStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard rollup $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round((attempts.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardRollup {
        return TrailerYardRollup(
            code = code,
            name = name,
            attempts = attempts,
            maximum = maximum,
            contactEmail = contactEmail,
            timezone = timezone,
            summary = summary,
            updatedAt = updatedAt,
            segment = segment,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a trailer yard rollup in status planned. */
fun newTrailerYardRollup(code: String, name: String): TrailerYardRollup {
    return TrailerYardRollup(
        code = code,
        name = name,
        attempts = 0,
        maximum = 0.0,
        contactEmail = "",
        timezone = "",
        summary = "",
        updatedAt = 0,
        segment = "",
        batchSize = 0,
        status = TrailerYardRollupStatus.PLANNED,
    )
}
