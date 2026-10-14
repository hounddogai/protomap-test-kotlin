package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched lease return forecast records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a batched lease return forecast is in its life cycle. */
enum class BatchedLeaseReturnForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A batched lease return forecast as the lease return team keeps it. */
data class BatchedLeaseReturnForecast(
    var code: String,
    var title: String,
    var version: Int,
    var margin: Double,
    var discount: Double,
    var quantity: Int,
    var priority: Int,
    var region: String,
    var status: BatchedLeaseReturnForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedLeaseReturnForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedLeaseReturnForecastStatus): Boolean {
        when (status) {
            BatchedLeaseReturnForecastStatus.PLANNED -> return next == BatchedLeaseReturnForecastStatus.COMPLETED || next == BatchedLeaseReturnForecastStatus.CANCELLED
            BatchedLeaseReturnForecastStatus.SCHEDULED -> return next == BatchedLeaseReturnForecastStatus.IN_PROGRESS || next == BatchedLeaseReturnForecastStatus.CANCELLED
            BatchedLeaseReturnForecastStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedLeaseReturnForecastStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedLeaseReturnForecastStatus.COMPLETED -> {
                when (next) {
                    BatchedLeaseReturnForecastStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedLeaseReturnForecastStatus) {
        if (!canMoveTo(next)) {
            error("batched lease return forecast $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun netExposure(): Double = round((priority.toDouble() + discount) * 100) / 100

    fun snapshot(): BatchedLeaseReturnForecast {
        return BatchedLeaseReturnForecast(
            code = code,
            title = title,
            version = version,
            margin = margin,
            discount = discount,
            quantity = quantity,
            priority = priority,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a batched lease return forecast in status planned. */
fun newBatchedLeaseReturnForecast(code: String, title: String): BatchedLeaseReturnForecast {
    return BatchedLeaseReturnForecast(
        code = code,
        title = title,
        version = 0,
        margin = 0.0,
        discount = 0.0,
        quantity = 0,
        priority = 0,
        region = "",
        status = BatchedLeaseReturnForecastStatus.PLANNED,
    )
}
