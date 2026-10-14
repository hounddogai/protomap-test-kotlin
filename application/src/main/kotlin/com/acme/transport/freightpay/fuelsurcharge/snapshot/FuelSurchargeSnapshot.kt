package com.acme.transport.freightpay.fuelsurcharge.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel surcharge snapshot records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a fuel surcharge snapshot is in its life cycle. */
enum class FuelSurchargeSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fuel surcharge snapshot as the fuel surcharge team keeps it. */
data class FuelSurchargeSnapshot(
    var code: String,
    var title: String,
    var floorValue: Double,
    var etaMinutes: Int,
    var distanceKm: Double,
    var pinned: Boolean,
    var threshold: Double,
    var cost: Double,
    var priority: Int,
    var status: FuelSurchargeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeSnapshotStatus): Boolean {
        when (status) {
            FuelSurchargeSnapshotStatus.IDLE -> return next == FuelSurchargeSnapshotStatus.DRAINING
            FuelSurchargeSnapshotStatus.WARMING -> return next == FuelSurchargeSnapshotStatus.READY || next == FuelSurchargeSnapshotStatus.STOPPED
            FuelSurchargeSnapshotStatus.READY -> return next == FuelSurchargeSnapshotStatus.STOPPED
            FuelSurchargeSnapshotStatus.DRAINING -> {
                when (next) {
                    FuelSurchargeSnapshotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedExposure(): Double = round((floorValue - priority.toDouble()) * 100) / 100

    fun snapshot(): FuelSurchargeSnapshot {
        return FuelSurchargeSnapshot(
            code = code,
            title = title,
            floorValue = floorValue,
            etaMinutes = etaMinutes,
            distanceKm = distanceKm,
            pinned = pinned,
            threshold = threshold,
            cost = cost,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel surcharge snapshot in status idle. */
fun newFuelSurchargeSnapshot(code: String, title: String): FuelSurchargeSnapshot {
    return FuelSurchargeSnapshot(
        code = code,
        title = title,
        floorValue = 0.0,
        etaMinutes = 0,
        distanceKm = 0.0,
        pinned = false,
        threshold = 0.0,
        cost = 0.0,
        priority = 0,
        status = FuelSurchargeSnapshotStatus.IDLE,
    )
}
