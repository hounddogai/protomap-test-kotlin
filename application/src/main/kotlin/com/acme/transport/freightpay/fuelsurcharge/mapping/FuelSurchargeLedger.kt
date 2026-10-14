package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.kit.text.normalize

/** Where a fuel surcharge ledger is in its life cycle. */
enum class FuelSurchargeLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a fuel surcharge ledger and its current status. */
data class FuelSurchargeLedger(
    var code: String,
    var name: String,
    var dock: String,
    var description: String,
    var summary: String,
    var segment: String,
    var score: Double,
    var status: FuelSurchargeLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeLedgerStatus): Boolean {
        when (status) {
            FuelSurchargeLedgerStatus.PLANNED -> return next == FuelSurchargeLedgerStatus.SCHEDULED || next == FuelSurchargeLedgerStatus.IN_PROGRESS
            FuelSurchargeLedgerStatus.SCHEDULED -> return next == FuelSurchargeLedgerStatus.IN_PROGRESS || next == FuelSurchargeLedgerStatus.COMPLETED
            FuelSurchargeLedgerStatus.IN_PROGRESS -> return next == FuelSurchargeLedgerStatus.COMPLETED || next == FuelSurchargeLedgerStatus.CANCELLED
            FuelSurchargeLedgerStatus.COMPLETED -> {
                when (next) {
                    FuelSurchargeLedgerStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeLedgerStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = score

    fun snapshot(): FuelSurchargeLedger {
        return FuelSurchargeLedger(
            code = code,
            name = name,
            dock = dock,
            description = description,
            summary = summary,
            segment = segment,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fuel surcharge ledger in status planned. */
fun newFuelSurchargeLedger(code: String, name: String): FuelSurchargeLedger {
    return FuelSurchargeLedger(
        code = code,
        name = name,
        dock = "",
        description = "",
        summary = "",
        segment = "",
        score = 0.0,
        status = FuelSurchargeLedgerStatus.PLANNED,
    )
}
