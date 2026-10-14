package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capacity ledger records of linehaul's capacity work, and the rules, stores, and calculations around
// them.

/** Where a capacity ledger is in its life cycle. */
enum class CapacityLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a capacity ledger and its current status. */
data class CapacityLedger(
    var code: String,
    var label: String,
    var revision: Int,
    var phone: String,
    var rate: Double,
    var pallets: Int,
    var etaMinutes: Int,
    var capacity: Int,
    var archived: Boolean,
    var carrier: String,
    var cost: Double,
    var status: CapacityLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityLedgerStatus): Boolean {
        when (status) {
            CapacityLedgerStatus.IDLE -> return next == CapacityLedgerStatus.WARMING
            CapacityLedgerStatus.WARMING -> return next == CapacityLedgerStatus.DRAINING
            CapacityLedgerStatus.READY -> return next == CapacityLedgerStatus.DRAINING || next == CapacityLedgerStatus.STOPPED
            CapacityLedgerStatus.DRAINING -> return next == CapacityLedgerStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CapacityLedgerStatus) {
        if (!canMoveTo(next)) {
            error("capacity ledger $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveValue(): Double = round(pallets.toDouble() * rate * 100) / 100

    fun snapshot(): CapacityLedger {
        return CapacityLedger(
            code = code,
            label = label,
            revision = revision,
            phone = phone,
            rate = rate,
            pallets = pallets,
            etaMinutes = etaMinutes,
            capacity = capacity,
            archived = archived,
            carrier = carrier,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capacity ledger in status idle. */
fun newCapacityLedger(code: String, label: String): CapacityLedger {
    return CapacityLedger(
        code = code,
        label = label,
        revision = 0,
        phone = "",
        rate = 0.0,
        pallets = 0,
        etaMinutes = 0,
        capacity = 0,
        archived = false,
        carrier = "",
        cost = 0.0,
        status = CapacityLedgerStatus.IDLE,
    )
}
