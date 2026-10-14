package com.acme.transport.network.transittime.timebatch

import com.acme.transport.kit.text.normalize

// The tiered transit time batch records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a tiered transit time batch is in its life cycle. */
enum class TieredTransitTimeBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tiered transit time batch. Its code is unique within a tenant. */
data class TieredTransitTimeBatch(
    var code: String,
    var name: String,
    var vehicleId: String,
    var quantity: Int,
    var timezone: String,
    var carrier: String,
    var status: TieredTransitTimeBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTransitTimeBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTransitTimeBatchStatus): Boolean {
        when (status) {
            TieredTransitTimeBatchStatus.PLANNED -> {
                when (next) {
                    TieredTransitTimeBatchStatus.COMPLETED, TieredTransitTimeBatchStatus.CANCELLED -> return true
                }
                return false
            }
            TieredTransitTimeBatchStatus.SCHEDULED -> return next == TieredTransitTimeBatchStatus.IN_PROGRESS || next == TieredTransitTimeBatchStatus.COMPLETED
            TieredTransitTimeBatchStatus.IN_PROGRESS -> {
                when (next) {
                    TieredTransitTimeBatchStatus.COMPLETED, TieredTransitTimeBatchStatus.CANCELLED -> return true
                }
                return false
            }
            TieredTransitTimeBatchStatus.COMPLETED -> return next == TieredTransitTimeBatchStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredTransitTimeBatchStatus) {
        if (!canMoveTo(next)) {
            error("tiered transit time batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): TieredTransitTimeBatch {
        return TieredTransitTimeBatch(
            code = code,
            name = name,
            vehicleId = vehicleId,
            quantity = quantity,
            timezone = timezone,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered transit time batch in status planned. */
fun newTieredTransitTimeBatch(code: String, name: String): TieredTransitTimeBatch {
    return TieredTransitTimeBatch(
        code = code,
        name = name,
        vehicleId = "",
        quantity = 0,
        timezone = "",
        carrier = "",
        status = TieredTransitTimeBatchStatus.PLANNED,
    )
}
