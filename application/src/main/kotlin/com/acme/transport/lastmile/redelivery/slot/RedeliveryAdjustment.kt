package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery adjustment is in its life cycle. */
enum class RedeliveryAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A redelivery adjustment. Its code is unique within a tenant. */
data class RedeliveryAdjustment(
    var code: String,
    var name: String,
    var timezone: String,
    var amount: Double,
    var margin: Double,
    var batchSize: Int,
    var vehicleId: String,
    var priority: Int,
    var status: RedeliveryAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryAdjustmentStatus): Boolean {
        when (status) {
            RedeliveryAdjustmentStatus.CREATED -> return next == RedeliveryAdjustmentStatus.RETIRED
            RedeliveryAdjustmentStatus.VALIDATED -> return next == RedeliveryAdjustmentStatus.RETIRED
            RedeliveryAdjustmentStatus.PUBLISHED -> return next == RedeliveryAdjustmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RedeliveryAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("redelivery adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round((amount - batchSize.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryAdjustment {
        return RedeliveryAdjustment(
            code = code,
            name = name,
            timezone = timezone,
            amount = amount,
            margin = margin,
            batchSize = batchSize,
            vehicleId = vehicleId,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a redelivery adjustment in status created. */
fun newRedeliveryAdjustment(code: String, name: String): RedeliveryAdjustment {
    return RedeliveryAdjustment(
        code = code,
        name = name,
        timezone = "",
        amount = 0.0,
        margin = 0.0,
        batchSize = 0,
        vehicleId = "",
        priority = 0,
        status = RedeliveryAdjustmentStatus.CREATED,
    )
}
