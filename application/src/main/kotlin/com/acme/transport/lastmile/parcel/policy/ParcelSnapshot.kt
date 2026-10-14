package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel snapshot is in its life cycle. */
enum class ParcelSnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a parcel snapshot and its current status. */
data class ParcelSnapshot(
    var code: String,
    var label: String,
    var name: String,
    var capacity: Int,
    var reviewer: String,
    var odometer: Int,
    var vehicleId: String,
    var trailerId: String,
    var status: ParcelSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelSnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelSnapshotStatus): Boolean {
        when (status) {
            ParcelSnapshotStatus.REQUESTED -> {
                when (next) {
                    ParcelSnapshotStatus.GRANTED -> return true
                }
                return false
            }
            ParcelSnapshotStatus.GRANTED -> {
                when (next) {
                    ParcelSnapshotStatus.REVOKED -> return true
                }
                return false
            }
            ParcelSnapshotStatus.REVOKED -> return next == ParcelSnapshotStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ParcelSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("parcel snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedLoad(): Double = round(capacity.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): ParcelSnapshot {
        return ParcelSnapshot(
            code = code,
            label = label,
            name = name,
            capacity = capacity,
            reviewer = reviewer,
            odometer = odometer,
            vehicleId = vehicleId,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a parcel snapshot in status requested. */
fun newParcelSnapshot(code: String, label: String): ParcelSnapshot {
    return ParcelSnapshot(
        code = code,
        label = label,
        name = "",
        capacity = 0,
        reviewer = "",
        odometer = 0,
        vehicleId = "",
        trailerId = "",
        status = ParcelSnapshotStatus.REQUESTED,
    )
}
