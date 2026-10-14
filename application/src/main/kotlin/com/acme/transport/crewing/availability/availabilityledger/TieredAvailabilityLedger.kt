package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered availability ledger records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a tiered availability ledger is in its life cycle. */
enum class TieredAvailabilityLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One tiered availability ledger, with what the planners need to act on it. */
data class TieredAvailabilityLedger(
    var code: String,
    var label: String,
    var priority: Int,
    var title: String,
    var expiresAt: Int,
    var dock: String,
    var segment: String,
    var loadKg: Double,
    var name: String,
    var attributes: MutableMap<String, String>,
    var status: TieredAvailabilityLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAvailabilityLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAvailabilityLedgerStatus): Boolean {
        when (status) {
            TieredAvailabilityLedgerStatus.OPEN -> {
                when (next) {
                    TieredAvailabilityLedgerStatus.RESOLVED, TieredAvailabilityLedgerStatus.REOPENED -> return true
                }
                return false
            }
            TieredAvailabilityLedgerStatus.IN_REVIEW -> return next == TieredAvailabilityLedgerStatus.REOPENED || next == TieredAvailabilityLedgerStatus.ARCHIVED
            TieredAvailabilityLedgerStatus.RESOLVED -> {
                when (next) {
                    TieredAvailabilityLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredAvailabilityLedgerStatus.REOPENED -> {
                when (next) {
                    TieredAvailabilityLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredAvailabilityLedgerStatus) {
        if (!canMoveTo(next)) {
            error("tiered availability ledger $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((expiresAt.toDouble() - loadKg) * 100) / 100

    fun snapshot(): TieredAvailabilityLedger {
        return TieredAvailabilityLedger(
            code = code,
            label = label,
            priority = priority,
            title = title,
            expiresAt = expiresAt,
            dock = dock,
            segment = segment,
            loadKg = loadKg,
            name = name,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered availability ledger in status open. */
fun newTieredAvailabilityLedger(code: String, label: String): TieredAvailabilityLedger {
    return TieredAvailabilityLedger(
        code = code,
        label = label,
        priority = 0,
        title = "",
        expiresAt = 0,
        dock = "",
        segment = "",
        loadKg = 0.0,
        name = "",
        attributes = mutableMapOf<String, String>(),
        status = TieredAvailabilityLedgerStatus.OPEN,
    )
}
