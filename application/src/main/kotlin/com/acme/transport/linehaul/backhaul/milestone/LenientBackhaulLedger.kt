package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.kit.text.normalize

/** Where a lenient backhaul ledger is in its life cycle. */
enum class LenientBackhaulLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a lenient backhaul ledger and its current status. */
data class LenientBackhaulLedger(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var unit: String,
    var shiftCode: String,
    var createdAt: Int,
    var status: LenientBackhaulLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientBackhaulLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientBackhaulLedgerStatus): Boolean {
        when (status) {
            LenientBackhaulLedgerStatus.DRAFT -> {
                when (next) {
                    LenientBackhaulLedgerStatus.ACTIVE -> return true
                }
                return false
            }
            LenientBackhaulLedgerStatus.PENDING -> {
                when (next) {
                    LenientBackhaulLedgerStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientBackhaulLedgerStatus.ACTIVE -> return next == LenientBackhaulLedgerStatus.SUSPENDED || next == LenientBackhaulLedgerStatus.CLOSED
            LenientBackhaulLedgerStatus.SUSPENDED -> return next == LenientBackhaulLedgerStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientBackhaulLedgerStatus) {
        if (!canMoveTo(next)) {
            error("lenient backhaul ledger $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): LenientBackhaulLedger {
        return LenientBackhaulLedger(
            code = code,
            name = name,
            attributes = attributes,
            unit = unit,
            shiftCode = shiftCode,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient backhaul ledger in status draft. */
fun newLenientBackhaulLedger(code: String, name: String): LenientBackhaulLedger {
    return LenientBackhaulLedger(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        unit = "",
        shiftCode = "",
        createdAt = 0,
        status = LenientBackhaulLedgerStatus.DRAFT,
    )
}
