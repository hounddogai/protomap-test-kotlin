package com.acme.transport.network.hub.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched hub claim is in its life cycle. */
enum class BatchedHubClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a batched hub claim and its current status. */
data class BatchedHubClaim(
    var code: String,
    var title: String,
    var attempts: Int,
    var phone: String,
    var score: Double,
    var revision: Int,
    var description: String,
    var rate: Double,
    var status: BatchedHubClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedHubClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedHubClaimStatus): Boolean {
        when (status) {
            BatchedHubClaimStatus.OPEN -> {
                when (next) {
                    BatchedHubClaimStatus.RESOLVED, BatchedHubClaimStatus.REOPENED -> return true
                }
                return false
            }
            BatchedHubClaimStatus.IN_REVIEW -> return next == BatchedHubClaimStatus.RESOLVED
            BatchedHubClaimStatus.RESOLVED -> return next == BatchedHubClaimStatus.REOPENED || next == BatchedHubClaimStatus.ARCHIVED
            BatchedHubClaimStatus.REOPENED -> return next == BatchedHubClaimStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BatchedHubClaimStatus) {
        if (!canMoveTo(next)) {
            error("batched hub claim $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"batched hub claim"} $code"

    fun adjustedLoad(): Double = round(score * revision.toDouble() * 100) / 100

    fun snapshot(): BatchedHubClaim {
        return BatchedHubClaim(
            code = code,
            title = title,
            attempts = attempts,
            phone = phone,
            score = score,
            revision = revision,
            description = description,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched hub claim in status open. */
fun newBatchedHubClaim(code: String, title: String): BatchedHubClaim {
    return BatchedHubClaim(
        code = code,
        title = title,
        attempts = 0,
        phone = "",
        score = 0.0,
        revision = 0,
        description = "",
        rate = 0.0,
        status = BatchedHubClaimStatus.OPEN,
    )
}
