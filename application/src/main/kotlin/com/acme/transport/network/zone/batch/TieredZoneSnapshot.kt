package com.acme.transport.network.zone.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered zone snapshot is in its life cycle. */
enum class TieredZoneSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered zone snapshot as the zone team keeps it. */
data class TieredZoneSnapshot(
    var code: String,
    var label: String,
    var capacity: Int,
    var contactEmail: String,
    var discount: Double,
    var phone: String,
    var odometer: Int,
    var batchSize: Int,
    var active: Boolean,
    var graceDays: Int,
    var amount: Double,
    var status: TieredZoneSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredZoneSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredZoneSnapshotStatus): Boolean {
        when (status) {
            TieredZoneSnapshotStatus.PROPOSED -> {
                when (next) {
                    TieredZoneSnapshotStatus.APPROVED, TieredZoneSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredZoneSnapshotStatus.APPROVED -> {
                when (next) {
                    TieredZoneSnapshotStatus.REJECTED, TieredZoneSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredZoneSnapshotStatus.REJECTED -> return next == TieredZoneSnapshotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredZoneSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("tiered zone snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tiered zone snapshot"} $code"

    fun weightedVolume(): Double = round((batchSize.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): TieredZoneSnapshot {
        return TieredZoneSnapshot(
            code = code,
            label = label,
            capacity = capacity,
            contactEmail = contactEmail,
            discount = discount,
            phone = phone,
            odometer = odometer,
            batchSize = batchSize,
            active = active,
            graceDays = graceDays,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered zone snapshot in status proposed. */
fun newTieredZoneSnapshot(code: String, label: String): TieredZoneSnapshot {
    return TieredZoneSnapshot(
        code = code,
        label = label,
        capacity = 0,
        contactEmail = "",
        discount = 0.0,
        phone = "",
        odometer = 0,
        batchSize = 0,
        active = false,
        graceDays = 0,
        amount = 0.0,
        status = TieredZoneSnapshotStatus.PROPOSED,
    )
}
