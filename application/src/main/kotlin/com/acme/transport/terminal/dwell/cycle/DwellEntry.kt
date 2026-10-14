package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell entry is in its life cycle. */
enum class DwellEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a dwell entry and its current status. */
data class DwellEntry(
    var code: String,
    var name: String,
    var lane: String,
    var unit: String,
    var trailerId: String,
    var checksum: String,
    var stopCount: Int,
    var ownerId: String,
    var quantity: Int,
    var batchSize: Int,
    var budget: Double,
    var status: DwellEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellEntryStatus): Boolean {
        when (status) {
            DwellEntryStatus.PROPOSED -> {
                when (next) {
                    DwellEntryStatus.APPROVED -> return true
                }
                return false
            }
            DwellEntryStatus.APPROVED -> return next == DwellEntryStatus.REJECTED
            DwellEntryStatus.REJECTED -> return next == DwellEntryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DwellEntryStatus) {
        if (!canMoveTo(next)) {
            error("dwell entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netTotal(): Double = round(stopCount.toDouble() * budget * 100) / 100

    fun snapshot(): DwellEntry {
        return DwellEntry(
            code = code,
            name = name,
            lane = lane,
            unit = unit,
            trailerId = trailerId,
            checksum = checksum,
            stopCount = stopCount,
            ownerId = ownerId,
            quantity = quantity,
            batchSize = batchSize,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a dwell entry in status proposed. */
fun newDwellEntry(code: String, name: String): DwellEntry {
    return DwellEntry(
        code = code,
        name = name,
        lane = "",
        unit = "",
        trailerId = "",
        checksum = "",
        stopCount = 0,
        ownerId = "",
        quantity = 0,
        batchSize = 0,
        budget = 0.0,
        status = DwellEntryStatus.PROPOSED,
    )
}
