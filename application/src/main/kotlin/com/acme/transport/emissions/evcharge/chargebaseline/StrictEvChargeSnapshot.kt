package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.kit.text.normalize

/** Where a strict ev charge snapshot is in its life cycle. */
enum class StrictEvChargeSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict ev charge snapshot. Its code is unique within a tenant. */
data class StrictEvChargeSnapshot(
    var code: String,
    var name: String,
    var shiftCode: String,
    var lane: String,
    var locale: String,
    var batchSize: Int,
    var currency: String,
    var unit: String,
    var status: StrictEvChargeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictEvChargeSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictEvChargeSnapshotStatus): Boolean {
        when (status) {
            StrictEvChargeSnapshotStatus.DRAFT -> {
                when (next) {
                    StrictEvChargeSnapshotStatus.CLOSED -> return true
                }
                return false
            }
            StrictEvChargeSnapshotStatus.PENDING -> {
                when (next) {
                    StrictEvChargeSnapshotStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictEvChargeSnapshotStatus.ACTIVE -> return next == StrictEvChargeSnapshotStatus.SUSPENDED
            StrictEvChargeSnapshotStatus.SUSPENDED -> return next == StrictEvChargeSnapshotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictEvChargeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("strict ev charge snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict ev charge snapshot"} $code"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): StrictEvChargeSnapshot {
        return StrictEvChargeSnapshot(
            code = code,
            name = name,
            shiftCode = shiftCode,
            lane = lane,
            locale = locale,
            batchSize = batchSize,
            currency = currency,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict ev charge snapshot in status draft. */
fun newStrictEvChargeSnapshot(code: String, name: String): StrictEvChargeSnapshot {
    return StrictEvChargeSnapshot(
        code = code,
        name = name,
        shiftCode = "",
        lane = "",
        locale = "",
        batchSize = 0,
        currency = "",
        unit = "",
        status = StrictEvChargeSnapshotStatus.DRAFT,
    )
}
