package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched fuel surcharge segment records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a batched fuel surcharge segment is in its life cycle. */
enum class BatchedFuelSurchargeSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a batched fuel surcharge segment and its current status. */
data class BatchedFuelSurchargeSegment(
    var code: String,
    var name: String,
    var summary: String,
    var label: String,
    var tags: MutableList<String>,
    var pallets: Int,
    var amount: Double,
    var status: BatchedFuelSurchargeSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedFuelSurchargeSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedFuelSurchargeSegmentStatus): Boolean {
        when (status) {
            BatchedFuelSurchargeSegmentStatus.REQUESTED -> {
                when (next) {
                    BatchedFuelSurchargeSegmentStatus.REVOKED -> return true
                }
                return false
            }
            BatchedFuelSurchargeSegmentStatus.GRANTED -> {
                when (next) {
                    BatchedFuelSurchargeSegmentStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedFuelSurchargeSegmentStatus.REVOKED -> {
                when (next) {
                    BatchedFuelSurchargeSegmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedFuelSurchargeSegmentStatus) {
        if (!canMoveTo(next)) {
            error("batched fuel surcharge segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"batched fuel surcharge segment"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round((amount - pallets.toDouble()) * 100) / 100

    fun snapshot(): BatchedFuelSurchargeSegment {
        return BatchedFuelSurchargeSegment(
            code = code,
            name = name,
            summary = summary,
            label = label,
            tags = tags,
            pallets = pallets,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched fuel surcharge segment in status requested. */
fun newBatchedFuelSurchargeSegment(code: String, name: String): BatchedFuelSurchargeSegment {
    return BatchedFuelSurchargeSegment(
        code = code,
        name = name,
        summary = "",
        label = "",
        tags = mutableListOf<String>(),
        pallets = 0,
        amount = 0.0,
        status = BatchedFuelSurchargeSegmentStatus.REQUESTED,
    )
}
