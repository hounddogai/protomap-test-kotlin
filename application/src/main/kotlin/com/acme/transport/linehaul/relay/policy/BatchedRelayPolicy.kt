package com.acme.transport.linehaul.relay.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched relay policy records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a batched relay policy is in its life cycle. */
enum class BatchedRelayPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched relay policy. Its code is unique within a tenant. */
data class BatchedRelayPolicy(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var pallets: Int,
    var ceiling: Double,
    var externalRef: String,
    var ownerId: String,
    var rate: Double,
    var category: String,
    var trailerId: String,
    var priority: Int,
    var status: BatchedRelayPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRelayPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRelayPolicyStatus): Boolean {
        when (status) {
            BatchedRelayPolicyStatus.IDLE -> {
                when (next) {
                    BatchedRelayPolicyStatus.DRAINING -> return true
                }
                return false
            }
            BatchedRelayPolicyStatus.WARMING -> return next == BatchedRelayPolicyStatus.DRAINING || next == BatchedRelayPolicyStatus.STOPPED
            BatchedRelayPolicyStatus.READY -> return next == BatchedRelayPolicyStatus.STOPPED
            BatchedRelayPolicyStatus.DRAINING -> return next == BatchedRelayPolicyStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedRelayPolicyStatus) {
        if (!canMoveTo(next)) {
            error("batched relay policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netTotal(): Double = round((rate - pallets.toDouble()) * 100) / 100

    fun snapshot(): BatchedRelayPolicy {
        return BatchedRelayPolicy(
            code = code,
            title = title,
            distanceKm = distanceKm,
            pallets = pallets,
            ceiling = ceiling,
            externalRef = externalRef,
            ownerId = ownerId,
            rate = rate,
            category = category,
            trailerId = trailerId,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a batched relay policy in status idle. */
fun newBatchedRelayPolicy(code: String, title: String): BatchedRelayPolicy {
    return BatchedRelayPolicy(
        code = code,
        title = title,
        distanceKm = 0.0,
        pallets = 0,
        ceiling = 0.0,
        externalRef = "",
        ownerId = "",
        rate = 0.0,
        category = "",
        trailerId = "",
        priority = 0,
        status = BatchedRelayPolicyStatus.IDLE,
    )
}
