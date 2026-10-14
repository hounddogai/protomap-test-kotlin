package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual idling snapshot is in its life cycle. */
enum class ManualIdlingSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual idling snapshot as the idling team keeps it. */
data class ManualIdlingSnapshot(
    var code: String,
    var label: String,
    var floorValue: Double,
    var shiftCode: String,
    var startsAt: Int,
    var notes: String,
    var dock: String,
    var status: ManualIdlingSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualIdlingSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualIdlingSnapshotStatus): Boolean {
        when (status) {
            ManualIdlingSnapshotStatus.DRAFT -> return next == ManualIdlingSnapshotStatus.SUSPENDED || next == ManualIdlingSnapshotStatus.CLOSED
            ManualIdlingSnapshotStatus.PENDING -> return next == ManualIdlingSnapshotStatus.ACTIVE || next == ManualIdlingSnapshotStatus.CLOSED
            ManualIdlingSnapshotStatus.ACTIVE -> {
                when (next) {
                    ManualIdlingSnapshotStatus.CLOSED -> return true
                }
                return false
            }
            ManualIdlingSnapshotStatus.SUSPENDED -> return next == ManualIdlingSnapshotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ManualIdlingSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("manual idling snapshot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round(startsAt.toDouble() * floorValue * 100) / 100

    fun snapshot(): ManualIdlingSnapshot {
        return ManualIdlingSnapshot(
            code = code,
            label = label,
            floorValue = floorValue,
            shiftCode = shiftCode,
            startsAt = startsAt,
            notes = notes,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual idling snapshot in status draft. */
fun newManualIdlingSnapshot(code: String, label: String): ManualIdlingSnapshot {
    return ManualIdlingSnapshot(
        code = code,
        label = label,
        floorValue = 0.0,
        shiftCode = "",
        startsAt = 0,
        notes = "",
        dock = "",
        status = ManualIdlingSnapshotStatus.DRAFT,
    )
}
