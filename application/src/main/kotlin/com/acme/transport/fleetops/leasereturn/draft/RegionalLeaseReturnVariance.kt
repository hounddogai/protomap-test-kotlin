package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional lease return variance is in its life cycle. */
enum class RegionalLeaseReturnVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A regional lease return variance as the lease return team keeps it. */
data class RegionalLeaseReturnVariance(
    var code: String,
    var title: String,
    var cost: Double,
    var depot: String,
    var currency: String,
    var ratio: Double,
    var maximum: Double,
    var score: Double,
    var quantity: Int,
    var source: String,
    var vehicleId: String,
    var status: RegionalLeaseReturnVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalLeaseReturnVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalLeaseReturnVarianceStatus): Boolean {
        when (status) {
            RegionalLeaseReturnVarianceStatus.IDLE -> return next == RegionalLeaseReturnVarianceStatus.READY || next == RegionalLeaseReturnVarianceStatus.DRAINING
            RegionalLeaseReturnVarianceStatus.WARMING -> return next == RegionalLeaseReturnVarianceStatus.STOPPED
            RegionalLeaseReturnVarianceStatus.READY -> {
                when (next) {
                    RegionalLeaseReturnVarianceStatus.DRAINING -> return true
                }
                return false
            }
            RegionalLeaseReturnVarianceStatus.DRAINING -> {
                when (next) {
                    RegionalLeaseReturnVarianceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalLeaseReturnVarianceStatus) {
        if (!canMoveTo(next)) {
            error("regional lease return variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round(score * ratio * 100) / 100

    fun snapshot(): RegionalLeaseReturnVariance {
        return RegionalLeaseReturnVariance(
            code = code,
            title = title,
            cost = cost,
            depot = depot,
            currency = currency,
            ratio = ratio,
            maximum = maximum,
            score = score,
            quantity = quantity,
            source = source,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a regional lease return variance in status idle. */
fun newRegionalLeaseReturnVariance(code: String, title: String): RegionalLeaseReturnVariance {
    return RegionalLeaseReturnVariance(
        code = code,
        title = title,
        cost = 0.0,
        depot = "",
        currency = "",
        ratio = 0.0,
        maximum = 0.0,
        score = 0.0,
        quantity = 0,
        source = "",
        vehicleId = "",
        status = RegionalLeaseReturnVarianceStatus.IDLE,
    )
}
