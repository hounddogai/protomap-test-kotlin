package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched attempt override records of lastmile's attempt work, and the rules, stores, and calculations
// around them.

/** Where a batched attempt override is in its life cycle. */
enum class BatchedAttemptOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a batched attempt override and its current status. */
data class BatchedAttemptOverride(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var durationMinutes: Int,
    var approved: Boolean,
    var startsAt: Int,
    var depot: String,
    var budget: Double,
    var currency: String,
    var priority: Int,
    var margin: Double,
    var status: BatchedAttemptOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAttemptOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAttemptOverrideStatus): Boolean {
        when (status) {
            BatchedAttemptOverrideStatus.OPEN -> return next == BatchedAttemptOverrideStatus.IN_REVIEW
            BatchedAttemptOverrideStatus.IN_REVIEW -> {
                when (next) {
                    BatchedAttemptOverrideStatus.RESOLVED, BatchedAttemptOverrideStatus.REOPENED -> return true
                }
                return false
            }
            BatchedAttemptOverrideStatus.RESOLVED -> {
                when (next) {
                    BatchedAttemptOverrideStatus.REOPENED, BatchedAttemptOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedAttemptOverrideStatus.REOPENED -> {
                when (next) {
                    BatchedAttemptOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedAttemptOverrideStatus) {
        if (!canMoveTo(next)) {
            error("batched attempt override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round(budget * priority.toDouble() * 100) / 100

    fun snapshot(): BatchedAttemptOverride {
        return BatchedAttemptOverride(
            code = code,
            label = label,
            tags = tags,
            durationMinutes = durationMinutes,
            approved = approved,
            startsAt = startsAt,
            depot = depot,
            budget = budget,
            currency = currency,
            priority = priority,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a batched attempt override in status open. */
fun newBatchedAttemptOverride(code: String, label: String): BatchedAttemptOverride {
    return BatchedAttemptOverride(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        durationMinutes = 0,
        approved = false,
        startsAt = 0,
        depot = "",
        budget = 0.0,
        currency = "",
        priority = 0,
        margin = 0.0,
        status = BatchedAttemptOverrideStatus.OPEN,
    )
}
