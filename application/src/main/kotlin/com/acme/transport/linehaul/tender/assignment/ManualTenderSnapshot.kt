package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual tender snapshot is in its life cycle. */
enum class ManualTenderSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a manual tender snapshot and its current status. */
data class ManualTenderSnapshot(
    var code: String,
    var label: String,
    var pallets: Int,
    var active: Boolean,
    var priority: Int,
    var title: String,
    var odometer: Int,
    var ratio: Double,
    var status: ManualTenderSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTenderSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTenderSnapshotStatus): Boolean {
        when (status) {
            ManualTenderSnapshotStatus.CREATED -> return next == ManualTenderSnapshotStatus.PUBLISHED
            ManualTenderSnapshotStatus.VALIDATED -> {
                when (next) {
                    ManualTenderSnapshotStatus.PUBLISHED -> return true
                }
                return false
            }
            ManualTenderSnapshotStatus.PUBLISHED -> {
                when (next) {
                    ManualTenderSnapshotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTenderSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("manual tender snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedTotal(): Double = round((pallets.toDouble() + ratio) * 100) / 100

    fun snapshot(): ManualTenderSnapshot {
        return ManualTenderSnapshot(
            code = code,
            label = label,
            pallets = pallets,
            active = active,
            priority = priority,
            title = title,
            odometer = odometer,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual tender snapshot in status created. */
fun newManualTenderSnapshot(code: String, label: String): ManualTenderSnapshot {
    return ManualTenderSnapshot(
        code = code,
        label = label,
        pallets = 0,
        active = false,
        priority = 0,
        title = "",
        odometer = 0,
        ratio = 0.0,
        status = ManualTenderSnapshotStatus.CREATED,
    )
}
