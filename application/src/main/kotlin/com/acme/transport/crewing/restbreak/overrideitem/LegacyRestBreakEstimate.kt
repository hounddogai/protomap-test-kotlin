package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy rest break estimate is in its life cycle. */
enum class LegacyRestBreakEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One legacy rest break estimate, with what the planners need to act on it. */
data class LegacyRestBreakEstimate(
    var code: String,
    var title: String,
    var label: String,
    var createdAt: Int,
    var reviewer: String,
    var loadKg: Double,
    var status: LegacyRestBreakEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRestBreakEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRestBreakEstimateStatus): Boolean {
        when (status) {
            LegacyRestBreakEstimateStatus.IDLE -> {
                when (next) {
                    LegacyRestBreakEstimateStatus.DRAINING -> return true
                }
                return false
            }
            LegacyRestBreakEstimateStatus.WARMING -> {
                when (next) {
                    LegacyRestBreakEstimateStatus.DRAINING -> return true
                }
                return false
            }
            LegacyRestBreakEstimateStatus.READY -> {
                when (next) {
                    LegacyRestBreakEstimateStatus.DRAINING, LegacyRestBreakEstimateStatus.STOPPED -> return true
                }
                return false
            }
            LegacyRestBreakEstimateStatus.DRAINING -> {
                when (next) {
                    LegacyRestBreakEstimateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyRestBreakEstimateStatus) {
        if (!canMoveTo(next)) {
            error("legacy rest break estimate $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"legacy rest break estimate"} $code"

    fun effectiveExposure(): Double = round((loadKg - createdAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyRestBreakEstimate {
        return LegacyRestBreakEstimate(
            code = code,
            title = title,
            label = label,
            createdAt = createdAt,
            reviewer = reviewer,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy rest break estimate in status idle. */
fun newLegacyRestBreakEstimate(code: String, title: String): LegacyRestBreakEstimate {
    return LegacyRestBreakEstimate(
        code = code,
        title = title,
        label = "",
        createdAt = 0,
        reviewer = "",
        loadKg = 0.0,
        status = LegacyRestBreakEstimateStatus.IDLE,
    )
}
