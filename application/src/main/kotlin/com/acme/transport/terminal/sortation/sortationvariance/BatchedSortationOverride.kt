package com.acme.transport.terminal.sortation.sortationvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched sortation override is in its life cycle. */
enum class BatchedSortationOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A batched sortation override. Its code is unique within a tenant. */
data class BatchedSortationOverride(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var lane: String,
    var timezone: String,
    var phone: String,
    var loadKg: Double,
    var graceDays: Int,
    var status: BatchedSortationOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedSortationOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedSortationOverrideStatus): Boolean {
        when (status) {
            BatchedSortationOverrideStatus.DRAFT -> return next == BatchedSortationOverrideStatus.CLOSED
            BatchedSortationOverrideStatus.PENDING -> {
                when (next) {
                    BatchedSortationOverrideStatus.SUSPENDED -> return true
                }
                return false
            }
            BatchedSortationOverrideStatus.ACTIVE -> return next == BatchedSortationOverrideStatus.SUSPENDED || next == BatchedSortationOverrideStatus.CLOSED
            BatchedSortationOverrideStatus.SUSPENDED -> return next == BatchedSortationOverrideStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BatchedSortationOverrideStatus) {
        if (!canMoveTo(next)) {
            error("batched sortation override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedVolume(): Double = round((loadKg + graceDays.toDouble()) * 100) / 100

    fun snapshot(): BatchedSortationOverride {
        return BatchedSortationOverride(
            code = code,
            label = label,
            tags = tags,
            lane = lane,
            timezone = timezone,
            phone = phone,
            loadKg = loadKg,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched sortation override in status draft. */
fun newBatchedSortationOverride(code: String, label: String): BatchedSortationOverride {
    return BatchedSortationOverride(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        lane = "",
        timezone = "",
        phone = "",
        loadKg = 0.0,
        graceDays = 0,
        status = BatchedSortationOverrideStatus.DRAFT,
    )
}
