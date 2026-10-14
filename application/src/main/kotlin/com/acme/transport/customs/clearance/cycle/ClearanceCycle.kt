package com.acme.transport.customs.clearance.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The clearance cycle records of customs's clearance work, and the rules, stores, and calculations around
// them.

/** Where a clearance cycle is in its life cycle. */
enum class ClearanceCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A clearance cycle as the clearance team keeps it. */
data class ClearanceCycle(
    var code: String,
    var label: String,
    var phone: String,
    var lane: String,
    var currency: String,
    var sequence: Int,
    var etaMinutes: Int,
    var status: ClearanceCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceCycleStatus): Boolean {
        when (status) {
            ClearanceCycleStatus.DRAFT -> return next == ClearanceCycleStatus.ACTIVE || next == ClearanceCycleStatus.CLOSED
            ClearanceCycleStatus.PENDING -> return next == ClearanceCycleStatus.CLOSED
            ClearanceCycleStatus.ACTIVE -> return next == ClearanceCycleStatus.CLOSED
            ClearanceCycleStatus.SUSPENDED -> return next == ClearanceCycleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ClearanceCycleStatus) {
        if (!canMoveTo(next)) {
            error("clearance cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netValue(): Double = round((etaMinutes.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): ClearanceCycle {
        return ClearanceCycle(
            code = code,
            label = label,
            phone = phone,
            lane = lane,
            currency = currency,
            sequence = sequence,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a clearance cycle in status draft. */
fun newClearanceCycle(code: String, label: String): ClearanceCycle {
    return ClearanceCycle(
        code = code,
        label = label,
        phone = "",
        lane = "",
        currency = "",
        sequence = 0,
        etaMinutes = 0,
        status = ClearanceCycleStatus.DRAFT,
    )
}
