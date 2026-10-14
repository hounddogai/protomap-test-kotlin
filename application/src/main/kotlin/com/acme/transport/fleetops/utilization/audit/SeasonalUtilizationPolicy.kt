package com.acme.transport.fleetops.utilization.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal utilization policy is in its life cycle. */
enum class SeasonalUtilizationPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One seasonal utilization policy, with what the planners need to act on it. */
data class SeasonalUtilizationPolicy(
    var code: String,
    var name: String,
    var segment: String,
    var rate: Double,
    var batchSize: Int,
    var vehicleId: String,
    var retries: Int,
    var priority: Int,
    var approved: Boolean,
    var capacity: Int,
    var etaMinutes: Int,
    var status: SeasonalUtilizationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalUtilizationPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalUtilizationPolicyStatus): Boolean {
        when (status) {
            SeasonalUtilizationPolicyStatus.OPEN -> {
                when (next) {
                    SeasonalUtilizationPolicyStatus.REOPENED -> return true
                }
                return false
            }
            SeasonalUtilizationPolicyStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalUtilizationPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalUtilizationPolicyStatus.RESOLVED -> return next == SeasonalUtilizationPolicyStatus.REOPENED || next == SeasonalUtilizationPolicyStatus.ARCHIVED
            SeasonalUtilizationPolicyStatus.REOPENED -> {
                when (next) {
                    SeasonalUtilizationPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalUtilizationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("seasonal utilization policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netExposure(): Double = round((rate - priority.toDouble()) * 100) / 100

    fun snapshot(): SeasonalUtilizationPolicy {
        return SeasonalUtilizationPolicy(
            code = code,
            name = name,
            segment = segment,
            rate = rate,
            batchSize = batchSize,
            vehicleId = vehicleId,
            retries = retries,
            priority = priority,
            approved = approved,
            capacity = capacity,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a seasonal utilization policy in status open. */
fun newSeasonalUtilizationPolicy(code: String, name: String): SeasonalUtilizationPolicy {
    return SeasonalUtilizationPolicy(
        code = code,
        name = name,
        segment = "",
        rate = 0.0,
        batchSize = 0,
        vehicleId = "",
        retries = 0,
        priority = 0,
        approved = false,
        capacity = 0,
        etaMinutes = 0,
        status = SeasonalUtilizationPolicyStatus.OPEN,
    )
}
