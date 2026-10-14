package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute claim is in its life cycle. */
enum class DisputeClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A dispute claim. Its code is unique within a tenant. */
data class DisputeClaim(
    var code: String,
    var label: String,
    var summary: String,
    var source: String,
    var quantity: Int,
    var version: Int,
    var country: String,
    var batchSize: Int,
    var category: String,
    var status: DisputeClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeClaimStatus): Boolean {
        when (status) {
            DisputeClaimStatus.IDLE -> return next == DisputeClaimStatus.STOPPED
            DisputeClaimStatus.WARMING -> return next == DisputeClaimStatus.READY || next == DisputeClaimStatus.STOPPED
            DisputeClaimStatus.READY -> return next == DisputeClaimStatus.STOPPED
            DisputeClaimStatus.DRAINING -> {
                when (next) {
                    DisputeClaimStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeClaimStatus) {
        if (!canMoveTo(next)) {
            error("dispute claim $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"dispute claim"} $code"

    fun adjustedLoad(): Double = round(batchSize.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): DisputeClaim {
        return DisputeClaim(
            code = code,
            label = label,
            summary = summary,
            source = source,
            quantity = quantity,
            version = version,
            country = country,
            batchSize = batchSize,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a dispute claim in status idle. */
fun newDisputeClaim(code: String, label: String): DisputeClaim {
    return DisputeClaim(
        code = code,
        label = label,
        summary = "",
        source = "",
        quantity = 0,
        version = 0,
        country = "",
        batchSize = 0,
        category = "",
        status = DisputeClaimStatus.IDLE,
    )
}
