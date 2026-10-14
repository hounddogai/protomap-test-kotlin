package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched broker claim is in its life cycle. */
enum class BatchedBrokerClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A batched broker claim. Its code is unique within a tenant. */
data class BatchedBrokerClaim(
    var code: String,
    var label: String,
    var region: String,
    var routeCode: String,
    var currency: String,
    var endsAt: Int,
    var segment: String,
    var floorValue: Double,
    var status: BatchedBrokerClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedBrokerClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedBrokerClaimStatus): Boolean {
        when (status) {
            BatchedBrokerClaimStatus.DRAFT -> {
                when (next) {
                    BatchedBrokerClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            BatchedBrokerClaimStatus.PENDING -> return next == BatchedBrokerClaimStatus.ACTIVE || next == BatchedBrokerClaimStatus.CLOSED
            BatchedBrokerClaimStatus.ACTIVE -> {
                when (next) {
                    BatchedBrokerClaimStatus.CLOSED -> return true
                }
                return false
            }
            BatchedBrokerClaimStatus.SUSPENDED -> return next == BatchedBrokerClaimStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BatchedBrokerClaimStatus) {
        if (!canMoveTo(next)) {
            error("batched broker claim $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"batched broker claim"} $code"

    fun projectedExposure(): Double = round((floorValue - endsAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedBrokerClaim {
        return BatchedBrokerClaim(
            code = code,
            label = label,
            region = region,
            routeCode = routeCode,
            currency = currency,
            endsAt = endsAt,
            segment = segment,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a batched broker claim in status draft. */
fun newBatchedBrokerClaim(code: String, label: String): BatchedBrokerClaim {
    return BatchedBrokerClaim(
        code = code,
        label = label,
        region = "",
        routeCode = "",
        currency = "",
        endsAt = 0,
        segment = "",
        floorValue = 0.0,
        status = BatchedBrokerClaimStatus.DRAFT,
    )
}
