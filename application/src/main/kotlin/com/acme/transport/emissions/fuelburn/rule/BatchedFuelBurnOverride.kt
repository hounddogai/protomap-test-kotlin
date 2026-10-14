package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched fuel burn override is in its life cycle. */
enum class BatchedFuelBurnOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched fuel burn override as the fuel burn team keeps it. */
data class BatchedFuelBurnOverride(
    var code: String,
    var name: String,
    var quantity: Int,
    var archived: Boolean,
    var endsAt: Int,
    var timezone: String,
    var status: BatchedFuelBurnOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedFuelBurnOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedFuelBurnOverrideStatus): Boolean {
        when (status) {
            BatchedFuelBurnOverrideStatus.CREATED -> return next == BatchedFuelBurnOverrideStatus.PUBLISHED
            BatchedFuelBurnOverrideStatus.VALIDATED -> return next == BatchedFuelBurnOverrideStatus.RETIRED
            BatchedFuelBurnOverrideStatus.PUBLISHED -> return next == BatchedFuelBurnOverrideStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedFuelBurnOverrideStatus) {
        if (!canMoveTo(next)) {
            error("batched fuel burn override $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedVolume(): Double = round((quantity.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedFuelBurnOverride {
        return BatchedFuelBurnOverride(
            code = code,
            name = name,
            quantity = quantity,
            archived = archived,
            endsAt = endsAt,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched fuel burn override in status created. */
fun newBatchedFuelBurnOverride(code: String, name: String): BatchedFuelBurnOverride {
    return BatchedFuelBurnOverride(
        code = code,
        name = name,
        quantity = 0,
        archived = false,
        endsAt = 0,
        timezone = "",
        status = BatchedFuelBurnOverrideStatus.CREATED,
    )
}
