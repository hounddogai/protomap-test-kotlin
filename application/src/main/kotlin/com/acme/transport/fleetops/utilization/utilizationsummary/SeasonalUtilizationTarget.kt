package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal utilization target is in its life cycle. */
enum class SeasonalUtilizationTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A seasonal utilization target as the utilization team keeps it. */
data class SeasonalUtilizationTarget(
    var code: String,
    var label: String,
    var revision: Int,
    var locale: String,
    var active: Boolean,
    var score: Double,
    var createdAt: Int,
    var depot: String,
    var etaMinutes: Int,
    var status: SeasonalUtilizationTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalUtilizationTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalUtilizationTargetStatus): Boolean {
        when (status) {
            SeasonalUtilizationTargetStatus.PROPOSED -> return next == SeasonalUtilizationTargetStatus.APPROVED || next == SeasonalUtilizationTargetStatus.WITHDRAWN
            SeasonalUtilizationTargetStatus.APPROVED -> {
                when (next) {
                    SeasonalUtilizationTargetStatus.REJECTED, SeasonalUtilizationTargetStatus.WITHDRAWN -> return true
                }
                return false
            }
            SeasonalUtilizationTargetStatus.REJECTED -> {
                when (next) {
                    SeasonalUtilizationTargetStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalUtilizationTargetStatus) {
        if (!canMoveTo(next)) {
            error("seasonal utilization target $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveTotal(): Double = round(etaMinutes.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): SeasonalUtilizationTarget {
        return SeasonalUtilizationTarget(
            code = code,
            label = label,
            revision = revision,
            locale = locale,
            active = active,
            score = score,
            createdAt = createdAt,
            depot = depot,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a seasonal utilization target in status proposed. */
fun newSeasonalUtilizationTarget(code: String, label: String): SeasonalUtilizationTarget {
    return SeasonalUtilizationTarget(
        code = code,
        label = label,
        revision = 0,
        locale = "",
        active = false,
        score = 0.0,
        createdAt = 0,
        depot = "",
        etaMinutes = 0,
        status = SeasonalUtilizationTargetStatus.PROPOSED,
    )
}
