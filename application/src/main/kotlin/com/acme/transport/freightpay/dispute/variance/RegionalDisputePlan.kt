package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional dispute plan is in its life cycle. */
enum class RegionalDisputePlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A regional dispute plan. Its code is unique within a tenant. */
data class RegionalDisputePlan(
    var code: String,
    var label: String,
    var vehicleId: String,
    var limit: Int,
    var attempts: Int,
    var margin: Double,
    var checksum: String,
    var country: String,
    var threshold: Double,
    var status: RegionalDisputePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDisputePlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDisputePlanStatus): Boolean {
        when (status) {
            RegionalDisputePlanStatus.PLANNED -> {
                when (next) {
                    RegionalDisputePlanStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalDisputePlanStatus.SCHEDULED -> {
                when (next) {
                    RegionalDisputePlanStatus.COMPLETED, RegionalDisputePlanStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalDisputePlanStatus.IN_PROGRESS -> return next == RegionalDisputePlanStatus.CANCELLED
            RegionalDisputePlanStatus.COMPLETED -> return next == RegionalDisputePlanStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RegionalDisputePlanStatus) {
        if (!canMoveTo(next)) {
            error("regional dispute plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional dispute plan"} $code"

    fun adjustedLoad(): Double = round((limit.toDouble() + margin) * 100) / 100

    fun snapshot(): RegionalDisputePlan {
        return RegionalDisputePlan(
            code = code,
            label = label,
            vehicleId = vehicleId,
            limit = limit,
            attempts = attempts,
            margin = margin,
            checksum = checksum,
            country = country,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a regional dispute plan in status planned. */
fun newRegionalDisputePlan(code: String, label: String): RegionalDisputePlan {
    return RegionalDisputePlan(
        code = code,
        label = label,
        vehicleId = "",
        limit = 0,
        attempts = 0,
        margin = 0.0,
        checksum = "",
        country = "",
        threshold = 0.0,
        status = RegionalDisputePlanStatus.PLANNED,
    )
}
