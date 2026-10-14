package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dispute policy records of freightpay's dispute work, and the rules, stores, and calculations around
// them.

/** Where a dispute policy is in its life cycle. */
enum class DisputePolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One dispute policy, with what the planners need to act on it. */
data class DisputePolicy(
    var code: String,
    var label: String,
    var driverId: String,
    var etaMinutes: Int,
    var country: String,
    var retries: Int,
    var version: Int,
    var locale: String,
    var vehicleId: String,
    var loadKg: Double,
    var status: DisputePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputePolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputePolicyStatus): Boolean {
        when (status) {
            DisputePolicyStatus.OPEN -> return next == DisputePolicyStatus.RESOLVED || next == DisputePolicyStatus.REOPENED
            DisputePolicyStatus.IN_REVIEW -> return next == DisputePolicyStatus.RESOLVED
            DisputePolicyStatus.RESOLVED -> {
                when (next) {
                    DisputePolicyStatus.ARCHIVED -> return true
                }
                return false
            }
            DisputePolicyStatus.REOPENED -> return next == DisputePolicyStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DisputePolicyStatus) {
        if (!canMoveTo(next)) {
            error("dispute policy $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedTotal(): Double = round(retries.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): DisputePolicy {
        return DisputePolicy(
            code = code,
            label = label,
            driverId = driverId,
            etaMinutes = etaMinutes,
            country = country,
            retries = retries,
            version = version,
            locale = locale,
            vehicleId = vehicleId,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a dispute policy in status open. */
fun newDisputePolicy(code: String, label: String): DisputePolicy {
    return DisputePolicy(
        code = code,
        label = label,
        driverId = "",
        etaMinutes = 0,
        country = "",
        retries = 0,
        version = 0,
        locale = "",
        vehicleId = "",
        loadKg = 0.0,
        status = DisputePolicyStatus.OPEN,
    )
}
