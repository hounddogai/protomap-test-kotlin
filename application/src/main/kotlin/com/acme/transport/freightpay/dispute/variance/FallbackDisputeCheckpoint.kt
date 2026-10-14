package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback dispute checkpoint is in its life cycle. */
enum class FallbackDisputeCheckpointStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One fallback dispute checkpoint, with what the planners need to act on it. */
data class FallbackDisputeCheckpoint(
    var code: String,
    var title: String,
    var cost: Double,
    var budget: Double,
    var contactEmail: String,
    var threshold: Double,
    var status: FallbackDisputeCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDisputeCheckpointStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDisputeCheckpointStatus): Boolean {
        when (status) {
            FallbackDisputeCheckpointStatus.NEW -> return next == FallbackDisputeCheckpointStatus.QUEUED || next == FallbackDisputeCheckpointStatus.DONE
            FallbackDisputeCheckpointStatus.QUEUED -> return next == FallbackDisputeCheckpointStatus.DONE
            FallbackDisputeCheckpointStatus.RUNNING -> return next == FallbackDisputeCheckpointStatus.FAILED
            FallbackDisputeCheckpointStatus.DONE -> return next == FallbackDisputeCheckpointStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackDisputeCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("fallback dispute checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedValue(): Double = round((cost + threshold) * 100) / 100

    fun snapshot(): FallbackDisputeCheckpoint {
        return FallbackDisputeCheckpoint(
            code = code,
            title = title,
            cost = cost,
            budget = budget,
            contactEmail = contactEmail,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback dispute checkpoint in status new. */
fun newFallbackDisputeCheckpoint(code: String, title: String): FallbackDisputeCheckpoint {
    return FallbackDisputeCheckpoint(
        code = code,
        title = title,
        cost = 0.0,
        budget = 0.0,
        contactEmail = "",
        threshold = 0.0,
        status = FallbackDisputeCheckpointStatus.NEW,
    )
}
