package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return threshold is in its life cycle. */
enum class LeaseReturnThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a lease return threshold and its current status. */
data class LeaseReturnThreshold(
    var code: String,
    var name: String,
    var discount: Double,
    var stopCount: Int,
    var rate: Double,
    var sequence: Int,
    var label: String,
    var batchSize: Int,
    var region: String,
    var quantity: Int,
    var status: LeaseReturnThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnThresholdStatus): Boolean {
        when (status) {
            LeaseReturnThresholdStatus.PROPOSED -> {
                when (next) {
                    LeaseReturnThresholdStatus.APPROVED, LeaseReturnThresholdStatus.REJECTED -> return true
                }
                return false
            }
            LeaseReturnThresholdStatus.APPROVED -> return next == LeaseReturnThresholdStatus.REJECTED || next == LeaseReturnThresholdStatus.WITHDRAWN
            LeaseReturnThresholdStatus.REJECTED -> {
                when (next) {
                    LeaseReturnThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnThresholdStatus) {
        if (!canMoveTo(next)) {
            error("lease return threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lease return threshold"} $code"

    fun netLoad(): Double = round((quantity.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): LeaseReturnThreshold {
        return LeaseReturnThreshold(
            code = code,
            name = name,
            discount = discount,
            stopCount = stopCount,
            rate = rate,
            sequence = sequence,
            label = label,
            batchSize = batchSize,
            region = region,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a lease return threshold in status proposed. */
fun newLeaseReturnThreshold(code: String, name: String): LeaseReturnThreshold {
    return LeaseReturnThreshold(
        code = code,
        name = name,
        discount = 0.0,
        stopCount = 0,
        rate = 0.0,
        sequence = 0,
        label = "",
        batchSize = 0,
        region = "",
        quantity = 0,
        status = LeaseReturnThresholdStatus.PROPOSED,
    )
}
