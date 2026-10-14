package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched availability tier records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a batched availability tier is in its life cycle. */
enum class BatchedAvailabilityTierStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched availability tier as the availability team keeps it. */
data class BatchedAvailabilityTier(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var segment: String,
    var retries: Int,
    var currency: String,
    var region: String,
    var pinned: Boolean,
    var status: BatchedAvailabilityTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAvailabilityTierStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAvailabilityTierStatus): Boolean {
        when (status) {
            BatchedAvailabilityTierStatus.CREATED -> {
                when (next) {
                    BatchedAvailabilityTierStatus.VALIDATED, BatchedAvailabilityTierStatus.PUBLISHED -> return true
                }
                return false
            }
            BatchedAvailabilityTierStatus.VALIDATED -> return next == BatchedAvailabilityTierStatus.PUBLISHED || next == BatchedAvailabilityTierStatus.RETIRED
            BatchedAvailabilityTierStatus.PUBLISHED -> return next == BatchedAvailabilityTierStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedAvailabilityTierStatus) {
        if (!canMoveTo(next)) {
            error("batched availability tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedVolume(): Double = round((durationMinutes.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): BatchedAvailabilityTier {
        return BatchedAvailabilityTier(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            segment = segment,
            retries = retries,
            currency = currency,
            region = region,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a batched availability tier in status created. */
fun newBatchedAvailabilityTier(code: String, name: String): BatchedAvailabilityTier {
    return BatchedAvailabilityTier(
        code = code,
        name = name,
        durationMinutes = 0,
        segment = "",
        retries = 0,
        currency = "",
        region = "",
        pinned = false,
        status = BatchedAvailabilityTierStatus.CREATED,
    )
}
