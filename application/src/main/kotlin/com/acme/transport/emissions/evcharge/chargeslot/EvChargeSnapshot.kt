package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge snapshot is in its life cycle. */
enum class EvChargeSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a ev charge snapshot and its current status. */
data class EvChargeSnapshot(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var fuelLiters: Double,
    var reviewer: String,
    var durationMinutes: Int,
    var status: EvChargeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeSnapshotStatus): Boolean {
        when (status) {
            EvChargeSnapshotStatus.CREATED -> return next == EvChargeSnapshotStatus.VALIDATED
            EvChargeSnapshotStatus.VALIDATED -> return next == EvChargeSnapshotStatus.PUBLISHED || next == EvChargeSnapshotStatus.RETIRED
            EvChargeSnapshotStatus.PUBLISHED -> return next == EvChargeSnapshotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: EvChargeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("ev charge snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round(durationMinutes.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): EvChargeSnapshot {
        return EvChargeSnapshot(
            code = code,
            label = label,
            attributes = attributes,
            fuelLiters = fuelLiters,
            reviewer = reviewer,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a ev charge snapshot in status created. */
fun newEvChargeSnapshot(code: String, label: String): EvChargeSnapshot {
    return EvChargeSnapshot(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        fuelLiters = 0.0,
        reviewer = "",
        durationMinutes = 0,
        status = EvChargeSnapshotStatus.CREATED,
    )
}
