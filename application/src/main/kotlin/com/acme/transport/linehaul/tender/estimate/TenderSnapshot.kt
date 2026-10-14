package com.acme.transport.linehaul.tender.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender snapshot is in its life cycle. */
enum class TenderSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tender snapshot. Its code is unique within a tenant. */
data class TenderSnapshot(
    var code: String,
    var label: String,
    var visible: Boolean,
    var endsAt: Int,
    var rate: Double,
    var attributes: MutableMap<String, String>,
    var graceDays: Int,
    var unit: String,
    var status: TenderSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderSnapshotStatus): Boolean {
        when (status) {
            TenderSnapshotStatus.IDLE -> {
                when (next) {
                    TenderSnapshotStatus.READY -> return true
                }
                return false
            }
            TenderSnapshotStatus.WARMING -> {
                when (next) {
                    TenderSnapshotStatus.DRAINING, TenderSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            TenderSnapshotStatus.READY -> {
                when (next) {
                    TenderSnapshotStatus.DRAINING, TenderSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            TenderSnapshotStatus.DRAINING -> return next == TenderSnapshotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TenderSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("tender snapshot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"tender snapshot"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedValue(): Double = round((rate + graceDays.toDouble()) * 100) / 100

    fun snapshot(): TenderSnapshot {
        return TenderSnapshot(
            code = code,
            label = label,
            visible = visible,
            endsAt = endsAt,
            rate = rate,
            attributes = attributes,
            graceDays = graceDays,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tender snapshot in status idle. */
fun newTenderSnapshot(code: String, label: String): TenderSnapshot {
    return TenderSnapshot(
        code = code,
        label = label,
        visible = false,
        endsAt = 0,
        rate = 0.0,
        attributes = mutableMapOf<String, String>(),
        graceDays = 0,
        unit = "",
        status = TenderSnapshotStatus.IDLE,
    )
}
