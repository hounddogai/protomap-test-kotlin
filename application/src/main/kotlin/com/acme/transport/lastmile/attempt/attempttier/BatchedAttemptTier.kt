package com.acme.transport.lastmile.attempt.attempttier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched attempt tier records of lastmile's attempt work, and the rules, stores, and calculations around
// them.

/** Where a batched attempt tier is in its life cycle. */
enum class BatchedAttemptTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One batched attempt tier, with what the planners need to act on it. */
data class BatchedAttemptTier(
    var code: String,
    var label: String,
    var endsAt: Int,
    var archived: Boolean,
    var fuelLiters: Double,
    var cost: Double,
    var trailerId: String,
    var source: String,
    var status: BatchedAttemptTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAttemptTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAttemptTierStatus): Boolean {
        when (status) {
            BatchedAttemptTierStatus.PROPOSED -> return next == BatchedAttemptTierStatus.APPROVED
            BatchedAttemptTierStatus.APPROVED -> {
                when (next) {
                    BatchedAttemptTierStatus.REJECTED -> return true
                }
                return false
            }
            BatchedAttemptTierStatus.REJECTED -> {
                when (next) {
                    BatchedAttemptTierStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedAttemptTierStatus) {
        if (!canMoveTo(next)) {
            error("batched attempt tier $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedValue(): Double = round((fuelLiters - endsAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedAttemptTier {
        return BatchedAttemptTier(
            code = code,
            label = label,
            endsAt = endsAt,
            archived = archived,
            fuelLiters = fuelLiters,
            cost = cost,
            trailerId = trailerId,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched attempt tier in status proposed. */
fun newBatchedAttemptTier(code: String, label: String): BatchedAttemptTier {
    return BatchedAttemptTier(
        code = code,
        label = label,
        endsAt = 0,
        archived = false,
        fuelLiters = 0.0,
        cost = 0.0,
        trailerId = "",
        source = "",
        status = BatchedAttemptTierStatus.PROPOSED,
    )
}
