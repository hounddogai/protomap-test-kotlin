package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient qualification claim is in its life cycle. */
enum class LenientQualificationClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One lenient qualification claim, with what the planners need to act on it. */
data class LenientQualificationClaim(
    var code: String,
    var name: String,
    var createdAt: Int,
    var score: Double,
    var channel: String,
    var updatedAt: Int,
    var status: LenientQualificationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientQualificationClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientQualificationClaimStatus): Boolean {
        when (status) {
            LenientQualificationClaimStatus.OPEN -> {
                when (next) {
                    LenientQualificationClaimStatus.RESOLVED, LenientQualificationClaimStatus.REOPENED -> return true
                }
                return false
            }
            LenientQualificationClaimStatus.IN_REVIEW -> return next == LenientQualificationClaimStatus.RESOLVED || next == LenientQualificationClaimStatus.ARCHIVED
            LenientQualificationClaimStatus.RESOLVED -> {
                when (next) {
                    LenientQualificationClaimStatus.REOPENED, LenientQualificationClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientQualificationClaimStatus.REOPENED -> {
                when (next) {
                    LenientQualificationClaimStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientQualificationClaimStatus) {
        if (!canMoveTo(next)) {
            error("lenient qualification claim $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient qualification claim"} $code"

    fun effectiveTotal(): Double = round((createdAt.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): LenientQualificationClaim {
        return LenientQualificationClaim(
            code = code,
            name = name,
            createdAt = createdAt,
            score = score,
            channel = channel,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lenient qualification claim in status open. */
fun newLenientQualificationClaim(code: String, name: String): LenientQualificationClaim {
    return LenientQualificationClaim(
        code = code,
        name = name,
        createdAt = 0,
        score = 0.0,
        channel = "",
        updatedAt = 0,
        status = LenientQualificationClaimStatus.OPEN,
    )
}
