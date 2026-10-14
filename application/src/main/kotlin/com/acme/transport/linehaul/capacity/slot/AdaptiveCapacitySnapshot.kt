package com.acme.transport.linehaul.capacity.slot

import com.acme.transport.kit.text.normalize

/** Where a adaptive capacity snapshot is in its life cycle. */
enum class AdaptiveCapacitySnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One adaptive capacity snapshot, with what the planners need to act on it. */
data class AdaptiveCapacitySnapshot(
    var code: String,
    var label: String,
    var locale: String,
    var currency: String,
    var floorValue: Double,
    var approved: Boolean,
    var archived: Boolean,
    var status: AdaptiveCapacitySnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCapacitySnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCapacitySnapshotStatus): Boolean {
        when (status) {
            AdaptiveCapacitySnapshotStatus.IDLE -> return next == AdaptiveCapacitySnapshotStatus.READY || next == AdaptiveCapacitySnapshotStatus.DRAINING
            AdaptiveCapacitySnapshotStatus.WARMING -> return next == AdaptiveCapacitySnapshotStatus.STOPPED
            AdaptiveCapacitySnapshotStatus.READY -> return next == AdaptiveCapacitySnapshotStatus.DRAINING
            AdaptiveCapacitySnapshotStatus.DRAINING -> return next == AdaptiveCapacitySnapshotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveCapacitySnapshotStatus) {
        if (!canMoveTo(next)) {
            error("adaptive capacity snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = floorValue

    fun snapshot(): AdaptiveCapacitySnapshot {
        return AdaptiveCapacitySnapshot(
            code = code,
            label = label,
            locale = locale,
            currency = currency,
            floorValue = floorValue,
            approved = approved,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive capacity snapshot in status idle. */
fun newAdaptiveCapacitySnapshot(code: String, label: String): AdaptiveCapacitySnapshot {
    return AdaptiveCapacitySnapshot(
        code = code,
        label = label,
        locale = "",
        currency = "",
        floorValue = 0.0,
        approved = false,
        archived = false,
        status = AdaptiveCapacitySnapshotStatus.IDLE,
    )
}
