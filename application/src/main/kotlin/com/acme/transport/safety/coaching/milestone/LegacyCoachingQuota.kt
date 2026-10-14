package com.acme.transport.safety.coaching.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy coaching quota is in its life cycle. */
enum class LegacyCoachingQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One legacy coaching quota, with what the planners need to act on it. */
data class LegacyCoachingQuota(
    var code: String,
    var label: String,
    var limit: Int,
    var region: String,
    var weight: Double,
    var version: Int,
    var stopCount: Int,
    var status: LegacyCoachingQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCoachingQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCoachingQuotaStatus): Boolean {
        when (status) {
            LegacyCoachingQuotaStatus.IDLE -> {
                when (next) {
                    LegacyCoachingQuotaStatus.READY -> return true
                }
                return false
            }
            LegacyCoachingQuotaStatus.WARMING -> {
                when (next) {
                    LegacyCoachingQuotaStatus.STOPPED -> return true
                }
                return false
            }
            LegacyCoachingQuotaStatus.READY -> {
                when (next) {
                    LegacyCoachingQuotaStatus.DRAINING, LegacyCoachingQuotaStatus.STOPPED -> return true
                }
                return false
            }
            LegacyCoachingQuotaStatus.DRAINING -> {
                when (next) {
                    LegacyCoachingQuotaStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyCoachingQuotaStatus) {
        if (!canMoveTo(next)) {
            error("legacy coaching quota $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"legacy coaching quota"} $code"

    fun projectedExposure(): Double = round((stopCount.toDouble() + weight) * 100) / 100

    fun snapshot(): LegacyCoachingQuota {
        return LegacyCoachingQuota(
            code = code,
            label = label,
            limit = limit,
            region = region,
            weight = weight,
            version = version,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a legacy coaching quota in status idle. */
fun newLegacyCoachingQuota(code: String, label: String): LegacyCoachingQuota {
    return LegacyCoachingQuota(
        code = code,
        label = label,
        limit = 0,
        region = "",
        weight = 0.0,
        version = 0,
        stopCount = 0,
        status = LegacyCoachingQuotaStatus.IDLE,
    )
}
