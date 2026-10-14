package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal violation claim is in its life cycle. */
enum class SeasonalViolationClaimStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One seasonal violation claim, with what the planners need to act on it. */
data class SeasonalViolationClaim(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var contactEmail: String,
    var batchSize: Int,
    var maximum: Double,
    var segment: String,
    var channel: String,
    var endsAt: Int,
    var pinned: Boolean,
    var graceDays: Int,
    var status: SeasonalViolationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalViolationClaimStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalViolationClaimStatus): Boolean {
        when (status) {
            SeasonalViolationClaimStatus.PROPOSED -> {
                when (next) {
                    SeasonalViolationClaimStatus.APPROVED -> return true
                }
                return false
            }
            SeasonalViolationClaimStatus.APPROVED -> return next == SeasonalViolationClaimStatus.WITHDRAWN
            SeasonalViolationClaimStatus.REJECTED -> return next == SeasonalViolationClaimStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: SeasonalViolationClaimStatus) {
        if (!canMoveTo(next)) {
            error("seasonal violation claim $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round((maximum - graceDays.toDouble()) * 100) / 100

    fun snapshot(): SeasonalViolationClaim {
        return SeasonalViolationClaim(
            code = code,
            title = title,
            tags = tags,
            contactEmail = contactEmail,
            batchSize = batchSize,
            maximum = maximum,
            segment = segment,
            channel = channel,
            endsAt = endsAt,
            pinned = pinned,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a seasonal violation claim in status proposed. */
fun newSeasonalViolationClaim(code: String, title: String): SeasonalViolationClaim {
    return SeasonalViolationClaim(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        contactEmail = "",
        batchSize = 0,
        maximum = 0.0,
        segment = "",
        channel = "",
        endsAt = 0,
        pinned = false,
        graceDays = 0,
        status = SeasonalViolationClaimStatus.PROPOSED,
    )
}
