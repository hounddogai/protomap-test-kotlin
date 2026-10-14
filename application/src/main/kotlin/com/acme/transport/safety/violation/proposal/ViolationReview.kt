package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation review is in its life cycle. */
enum class ViolationReviewStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One violation review, with what the planners need to act on it. */
data class ViolationReview(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var checksum: String,
    var createdAt: Int,
    var carrier: String,
    var status: ViolationReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationReviewStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationReviewStatus): Boolean {
        when (status) {
            ViolationReviewStatus.OPEN -> {
                when (next) {
                    ViolationReviewStatus.IN_REVIEW, ViolationReviewStatus.RESOLVED -> return true
                }
                return false
            }
            ViolationReviewStatus.IN_REVIEW -> return next == ViolationReviewStatus.RESOLVED || next == ViolationReviewStatus.ARCHIVED
            ViolationReviewStatus.RESOLVED -> {
                when (next) {
                    ViolationReviewStatus.REOPENED, ViolationReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            ViolationReviewStatus.REOPENED -> return next == ViolationReviewStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ViolationReviewStatus) {
        if (!canMoveTo(next)) {
            error("violation review $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun projectedVolume(): Double = round(expiresAt.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): ViolationReview {
        return ViolationReview(
            code = code,
            name = name,
            expiresAt = expiresAt,
            checksum = checksum,
            createdAt = createdAt,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a violation review in status open. */
fun newViolationReview(code: String, name: String): ViolationReview {
    return ViolationReview(
        code = code,
        name = name,
        expiresAt = 0,
        checksum = "",
        createdAt = 0,
        carrier = "",
        status = ViolationReviewStatus.OPEN,
    )
}
