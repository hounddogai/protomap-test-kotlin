package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery batch is in its life cycle. */
enum class RedeliveryBatchStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One redelivery batch, with what the planners need to act on it. */
data class RedeliveryBatch(
    var code: String,
    var name: String,
    var budget: Double,
    var routeCode: String,
    var updatedAt: Int,
    var amount: Double,
    var contactEmail: String,
    var archived: Boolean,
    var pallets: Int,
    var checksum: String,
    var status: RedeliveryBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryBatchStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryBatchStatus): Boolean {
        when (status) {
            RedeliveryBatchStatus.IDLE -> {
                when (next) {
                    RedeliveryBatchStatus.READY, RedeliveryBatchStatus.DRAINING -> return true
                }
                return false
            }
            RedeliveryBatchStatus.WARMING -> return next == RedeliveryBatchStatus.DRAINING
            RedeliveryBatchStatus.READY -> {
                when (next) {
                    RedeliveryBatchStatus.STOPPED -> return true
                }
                return false
            }
            RedeliveryBatchStatus.DRAINING -> {
                when (next) {
                    RedeliveryBatchStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryBatchStatus) {
        if (!canMoveTo(next)) {
            error("redelivery batch $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"redelivery batch"} $code"

    fun netValue(): Double = round((budget + pallets.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryBatch {
        return RedeliveryBatch(
            code = code,
            name = name,
            budget = budget,
            routeCode = routeCode,
            updatedAt = updatedAt,
            amount = amount,
            contactEmail = contactEmail,
            archived = archived,
            pallets = pallets,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a redelivery batch in status idle. */
fun newRedeliveryBatch(code: String, name: String): RedeliveryBatch {
    return RedeliveryBatch(
        code = code,
        name = name,
        budget = 0.0,
        routeCode = "",
        updatedAt = 0,
        amount = 0.0,
        contactEmail = "",
        archived = false,
        pallets = 0,
        checksum = "",
        status = RedeliveryBatchStatus.IDLE,
    )
}
