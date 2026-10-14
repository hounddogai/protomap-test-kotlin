package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal trailer yard policy is in its life cycle. */
enum class SeasonalTrailerYardPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One seasonal trailer yard policy, with what the planners need to act on it. */
data class SeasonalTrailerYardPolicy(
    var code: String,
    var label: String,
    var archived: Boolean,
    var sequence: Int,
    var windowDays: Int,
    var region: String,
    var approved: Boolean,
    var channel: String,
    var limit: Int,
    var status: SeasonalTrailerYardPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTrailerYardPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTrailerYardPolicyStatus): Boolean {
        when (status) {
            SeasonalTrailerYardPolicyStatus.REQUESTED -> return next == SeasonalTrailerYardPolicyStatus.EXPIRED
            SeasonalTrailerYardPolicyStatus.GRANTED -> {
                when (next) {
                    SeasonalTrailerYardPolicyStatus.REVOKED, SeasonalTrailerYardPolicyStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalTrailerYardPolicyStatus.REVOKED -> return next == SeasonalTrailerYardPolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalTrailerYardPolicyStatus) {
        if (!canMoveTo(next)) {
            error("seasonal trailer yard policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedValue(): Double = round(limit.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): SeasonalTrailerYardPolicy {
        return SeasonalTrailerYardPolicy(
            code = code,
            label = label,
            archived = archived,
            sequence = sequence,
            windowDays = windowDays,
            region = region,
            approved = approved,
            channel = channel,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a seasonal trailer yard policy in status requested. */
fun newSeasonalTrailerYardPolicy(code: String, label: String): SeasonalTrailerYardPolicy {
    return SeasonalTrailerYardPolicy(
        code = code,
        label = label,
        archived = false,
        sequence = 0,
        windowDays = 0,
        region = "",
        approved = false,
        channel = "",
        limit = 0,
        status = SeasonalTrailerYardPolicyStatus.REQUESTED,
    )
}
