package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal hub claim is in its life cycle. */
enum class SeasonalHubClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal hub claim as the hub team keeps it. */
data class SeasonalHubClaim(
    var code: String,
    var title: String,
    var graceDays: Int,
    var channel: String,
    var country: String,
    var unit: String,
    var summary: String,
    var segment: String,
    var amount: Double,
    var batchSize: Int,
    var status: SeasonalHubClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalHubClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalHubClaimStatus): Boolean {
        when (status) {
            SeasonalHubClaimStatus.IDLE -> {
                when (next) {
                    SeasonalHubClaimStatus.READY -> return true
                }
                return false
            }
            SeasonalHubClaimStatus.WARMING -> {
                when (next) {
                    SeasonalHubClaimStatus.DRAINING, SeasonalHubClaimStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalHubClaimStatus.READY -> return next == SeasonalHubClaimStatus.DRAINING
            SeasonalHubClaimStatus.DRAINING -> return next == SeasonalHubClaimStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SeasonalHubClaimStatus) {
        if (!canMoveTo(next)) {
            error("seasonal hub claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveTotal(): Double = round((batchSize.toDouble() - amount) * 100) / 100

    fun snapshot(): SeasonalHubClaim {
        return SeasonalHubClaim(
            code = code,
            title = title,
            graceDays = graceDays,
            channel = channel,
            country = country,
            unit = unit,
            summary = summary,
            segment = segment,
            amount = amount,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a seasonal hub claim in status idle. */
fun newSeasonalHubClaim(code: String, title: String): SeasonalHubClaim {
    return SeasonalHubClaim(
        code = code,
        title = title,
        graceDays = 0,
        channel = "",
        country = "",
        unit = "",
        summary = "",
        segment = "",
        amount = 0.0,
        batchSize = 0,
        status = SeasonalHubClaimStatus.IDLE,
    )
}
