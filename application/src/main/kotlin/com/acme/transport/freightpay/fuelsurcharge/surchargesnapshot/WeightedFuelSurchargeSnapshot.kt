package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted fuel surcharge snapshot records of freightpay's fuel surcharge work, and the rules, stores,
// and calculations around them.

/** Where a weighted fuel surcharge snapshot is in its life cycle. */
enum class WeightedFuelSurchargeSnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One weighted fuel surcharge snapshot, with what the planners need to act on it. */
data class WeightedFuelSurchargeSnapshot(
    var code: String,
    var label: String,
    var capacity: Int,
    var createdAt: Int,
    var batchSize: Int,
    var description: String,
    var depot: String,
    var tags: MutableList<String>,
    var timezone: String,
    var status: WeightedFuelSurchargeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFuelSurchargeSnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFuelSurchargeSnapshotStatus): Boolean {
        when (status) {
            WeightedFuelSurchargeSnapshotStatus.OPEN -> return next == WeightedFuelSurchargeSnapshotStatus.IN_REVIEW || next == WeightedFuelSurchargeSnapshotStatus.REOPENED
            WeightedFuelSurchargeSnapshotStatus.IN_REVIEW -> return next == WeightedFuelSurchargeSnapshotStatus.RESOLVED || next == WeightedFuelSurchargeSnapshotStatus.ARCHIVED
            WeightedFuelSurchargeSnapshotStatus.RESOLVED -> {
                when (next) {
                    WeightedFuelSurchargeSnapshotStatus.REOPENED, WeightedFuelSurchargeSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedFuelSurchargeSnapshotStatus.REOPENED -> return next == WeightedFuelSurchargeSnapshotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: WeightedFuelSurchargeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("weighted fuel surcharge snapshot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted fuel surcharge snapshot"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round(capacity.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): WeightedFuelSurchargeSnapshot {
        return WeightedFuelSurchargeSnapshot(
            code = code,
            label = label,
            capacity = capacity,
            createdAt = createdAt,
            batchSize = batchSize,
            description = description,
            depot = depot,
            tags = tags,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a weighted fuel surcharge snapshot in status open. */
fun newWeightedFuelSurchargeSnapshot(code: String, label: String): WeightedFuelSurchargeSnapshot {
    return WeightedFuelSurchargeSnapshot(
        code = code,
        label = label,
        capacity = 0,
        createdAt = 0,
        batchSize = 0,
        description = "",
        depot = "",
        tags = mutableListOf<String>(),
        timezone = "",
        status = WeightedFuelSurchargeSnapshotStatus.OPEN,
    )
}
