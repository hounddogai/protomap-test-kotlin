package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback scorecard quota is in its life cycle. */
enum class FallbackScorecardQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback scorecard quota as the scorecard team keeps it. */
data class FallbackScorecardQuota(
    var code: String,
    var label: String,
    var score: Double,
    var distanceKm: Double,
    var title: String,
    var stopCount: Int,
    var timezone: String,
    var segment: String,
    var status: FallbackScorecardQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackScorecardQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackScorecardQuotaStatus): Boolean {
        when (status) {
            FallbackScorecardQuotaStatus.IDLE -> {
                when (next) {
                    FallbackScorecardQuotaStatus.DRAINING -> return true
                }
                return false
            }
            FallbackScorecardQuotaStatus.WARMING -> return next == FallbackScorecardQuotaStatus.STOPPED
            FallbackScorecardQuotaStatus.READY -> {
                when (next) {
                    FallbackScorecardQuotaStatus.DRAINING -> return true
                }
                return false
            }
            FallbackScorecardQuotaStatus.DRAINING -> {
                when (next) {
                    FallbackScorecardQuotaStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackScorecardQuotaStatus) {
        if (!canMoveTo(next)) {
            error("fallback scorecard quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback scorecard quota"} $code"

    fun adjustedExposure(): Double = round(score * distanceKm * 100) / 100

    fun snapshot(): FallbackScorecardQuota {
        return FallbackScorecardQuota(
            code = code,
            label = label,
            score = score,
            distanceKm = distanceKm,
            title = title,
            stopCount = stopCount,
            timezone = timezone,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fallback scorecard quota in status idle. */
fun newFallbackScorecardQuota(code: String, label: String): FallbackScorecardQuota {
    return FallbackScorecardQuota(
        code = code,
        label = label,
        score = 0.0,
        distanceKm = 0.0,
        title = "",
        stopCount = 0,
        timezone = "",
        segment = "",
        status = FallbackScorecardQuotaStatus.IDLE,
    )
}
