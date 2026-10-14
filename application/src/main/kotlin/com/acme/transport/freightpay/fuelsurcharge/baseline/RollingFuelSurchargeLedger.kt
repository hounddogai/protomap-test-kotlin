package com.acme.transport.freightpay.fuelsurcharge.baseline

import com.acme.transport.kit.text.normalize

/** Where a rolling fuel surcharge ledger is in its life cycle. */
enum class RollingFuelSurchargeLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling fuel surcharge ledger. Its code is unique within a tenant. */
data class RollingFuelSurchargeLedger(
    var code: String,
    var label: String,
    var summary: String,
    var attributes: MutableMap<String, String>,
    var timezone: String,
    var phone: String,
    var dock: String,
    var shiftCode: String,
    var lane: String,
    var depot: String,
    var quantity: Int,
    var status: RollingFuelSurchargeLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFuelSurchargeLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFuelSurchargeLedgerStatus): Boolean {
        when (status) {
            RollingFuelSurchargeLedgerStatus.IDLE -> return next == RollingFuelSurchargeLedgerStatus.READY
            RollingFuelSurchargeLedgerStatus.WARMING -> {
                when (next) {
                    RollingFuelSurchargeLedgerStatus.DRAINING -> return true
                }
                return false
            }
            RollingFuelSurchargeLedgerStatus.READY -> {
                when (next) {
                    RollingFuelSurchargeLedgerStatus.DRAINING, RollingFuelSurchargeLedgerStatus.STOPPED -> return true
                }
                return false
            }
            RollingFuelSurchargeLedgerStatus.DRAINING -> return next == RollingFuelSurchargeLedgerStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingFuelSurchargeLedgerStatus) {
        if (!canMoveTo(next)) {
            error("rolling fuel surcharge ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): RollingFuelSurchargeLedger {
        return RollingFuelSurchargeLedger(
            code = code,
            label = label,
            summary = summary,
            attributes = attributes,
            timezone = timezone,
            phone = phone,
            dock = dock,
            shiftCode = shiftCode,
            lane = lane,
            depot = depot,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rolling fuel surcharge ledger in status idle. */
fun newRollingFuelSurchargeLedger(code: String, label: String): RollingFuelSurchargeLedger {
    return RollingFuelSurchargeLedger(
        code = code,
        label = label,
        summary = "",
        attributes = mutableMapOf<String, String>(),
        timezone = "",
        phone = "",
        dock = "",
        shiftCode = "",
        lane = "",
        depot = "",
        quantity = 0,
        status = RollingFuelSurchargeLedgerStatus.IDLE,
    )
}
