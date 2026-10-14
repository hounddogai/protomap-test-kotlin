package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal attempt estimate is in its life cycle. */
enum class SeasonalAttemptEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A seasonal attempt estimate as the attempt team keeps it. */
data class SeasonalAttemptEstimate(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var etaMinutes: Int,
    var channel: String,
    var tags: MutableList<String>,
    var currency: String,
    var status: SeasonalAttemptEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAttemptEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAttemptEstimateStatus): Boolean {
        when (status) {
            SeasonalAttemptEstimateStatus.PROPOSED -> return next == SeasonalAttemptEstimateStatus.APPROVED || next == SeasonalAttemptEstimateStatus.WITHDRAWN
            SeasonalAttemptEstimateStatus.APPROVED -> return next == SeasonalAttemptEstimateStatus.REJECTED || next == SeasonalAttemptEstimateStatus.WITHDRAWN
            SeasonalAttemptEstimateStatus.REJECTED -> {
                when (next) {
                    SeasonalAttemptEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalAttemptEstimateStatus) {
        if (!canMoveTo(next)) {
            error("seasonal attempt estimate $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round((updatedAt.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): SeasonalAttemptEstimate {
        return SeasonalAttemptEstimate(
            code = code,
            title = title,
            updatedAt = updatedAt,
            etaMinutes = etaMinutes,
            channel = channel,
            tags = tags,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a seasonal attempt estimate in status proposed. */
fun newSeasonalAttemptEstimate(code: String, title: String): SeasonalAttemptEstimate {
    return SeasonalAttemptEstimate(
        code = code,
        title = title,
        updatedAt = 0,
        etaMinutes = 0,
        channel = "",
        tags = mutableListOf<String>(),
        currency = "",
        status = SeasonalAttemptEstimateStatus.PROPOSED,
    )
}
