package com.acme.transport.fleetops.tire.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched tire tier is in its life cycle. */
enum class BatchedTireTierStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched tire tier as the tire team keeps it. */
data class BatchedTireTier(
    var code: String,
    var name: String,
    var pallets: Int,
    var fuelLiters: Double,
    var retries: Int,
    var depot: String,
    var phone: String,
    var pinned: Boolean,
    var status: BatchedTireTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTireTierStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTireTierStatus): Boolean {
        when (status) {
            BatchedTireTierStatus.CREATED -> {
                when (next) {
                    BatchedTireTierStatus.RETIRED -> return true
                }
                return false
            }
            BatchedTireTierStatus.VALIDATED -> {
                when (next) {
                    BatchedTireTierStatus.PUBLISHED, BatchedTireTierStatus.RETIRED -> return true
                }
                return false
            }
            BatchedTireTierStatus.PUBLISHED -> {
                when (next) {
                    BatchedTireTierStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTireTierStatus) {
        if (!canMoveTo(next)) {
            error("batched tire tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round((retries.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): BatchedTireTier {
        return BatchedTireTier(
            code = code,
            name = name,
            pallets = pallets,
            fuelLiters = fuelLiters,
            retries = retries,
            depot = depot,
            phone = phone,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a batched tire tier in status created. */
fun newBatchedTireTier(code: String, name: String): BatchedTireTier {
    return BatchedTireTier(
        code = code,
        name = name,
        pallets = 0,
        fuelLiters = 0.0,
        retries = 0,
        depot = "",
        phone = "",
        pinned = false,
        status = BatchedTireTierStatus.CREATED,
    )
}
