package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The carbon report policy records of emissions's carbon report work, and the rules, stores, and calculations
// around them.

/** Where a carbon report policy is in its life cycle. */
enum class CarbonReportPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One carbon report policy, with what the planners need to act on it. */
data class CarbonReportPolicy(
    var code: String,
    var name: String,
    var attempts: Int,
    var description: String,
    var currency: String,
    var batchSize: Int,
    var revision: Int,
    var stopCount: Int,
    var loadKg: Double,
    var lane: String,
    var status: CarbonReportPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportPolicyStatus): Boolean {
        when (status) {
            CarbonReportPolicyStatus.PROPOSED -> return next == CarbonReportPolicyStatus.APPROVED || next == CarbonReportPolicyStatus.WITHDRAWN
            CarbonReportPolicyStatus.APPROVED -> return next == CarbonReportPolicyStatus.REJECTED || next == CarbonReportPolicyStatus.WITHDRAWN
            CarbonReportPolicyStatus.REJECTED -> return next == CarbonReportPolicyStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CarbonReportPolicyStatus) {
        if (!canMoveTo(next)) {
            error("carbon report policy $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun projectedLoad(): Double = round((attempts.toDouble() + loadKg) * 100) / 100

    fun snapshot(): CarbonReportPolicy {
        return CarbonReportPolicy(
            code = code,
            name = name,
            attempts = attempts,
            description = description,
            currency = currency,
            batchSize = batchSize,
            revision = revision,
            stopCount = stopCount,
            loadKg = loadKg,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a carbon report policy in status proposed. */
fun newCarbonReportPolicy(code: String, name: String): CarbonReportPolicy {
    return CarbonReportPolicy(
        code = code,
        name = name,
        attempts = 0,
        description = "",
        currency = "",
        batchSize = 0,
        revision = 0,
        stopCount = 0,
        loadKg = 0.0,
        lane = "",
        status = CarbonReportPolicyStatus.PROPOSED,
    )
}
