package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard milestone is in its life cycle. */
enum class TrailerYardMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One trailer yard milestone, with what the planners need to act on it. */
data class TrailerYardMilestone(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var discount: Double,
    var currency: String,
    var score: Double,
    var expiresAt: Int,
    var windowDays: Int,
    var quantity: Int,
    var title: String,
    var priority: Int,
    var status: TrailerYardMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardMilestoneStatus): Boolean {
        when (status) {
            TrailerYardMilestoneStatus.DRAFT -> return next == TrailerYardMilestoneStatus.PENDING
            TrailerYardMilestoneStatus.PENDING -> {
                when (next) {
                    TrailerYardMilestoneStatus.SUSPENDED -> return true
                }
                return false
            }
            TrailerYardMilestoneStatus.ACTIVE -> {
                when (next) {
                    TrailerYardMilestoneStatus.SUSPENDED, TrailerYardMilestoneStatus.CLOSED -> return true
                }
                return false
            }
            TrailerYardMilestoneStatus.SUSPENDED -> {
                when (next) {
                    TrailerYardMilestoneStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard milestone $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round((durationMinutes.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardMilestone {
        return TrailerYardMilestone(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            discount = discount,
            currency = currency,
            score = score,
            expiresAt = expiresAt,
            windowDays = windowDays,
            quantity = quantity,
            title = title,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a trailer yard milestone in status draft. */
fun newTrailerYardMilestone(code: String, name: String): TrailerYardMilestone {
    return TrailerYardMilestone(
        code = code,
        name = name,
        durationMinutes = 0,
        discount = 0.0,
        currency = "",
        score = 0.0,
        expiresAt = 0,
        windowDays = 0,
        quantity = 0,
        title = "",
        priority = 0,
        status = TrailerYardMilestoneStatus.DRAFT,
    )
}
