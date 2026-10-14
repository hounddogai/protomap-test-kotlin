package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scorecard ledger records of safety's scorecard work, and the rules, stores, and calculations around
// them.

/** Where a scorecard ledger is in its life cycle. */
enum class ScorecardLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a scorecard ledger and its current status. */
data class ScorecardLedger(
    var code: String,
    var title: String,
    var limit: Int,
    var floorValue: Double,
    var category: String,
    var quantity: Int,
    var contactEmail: String,
    var attributes: MutableMap<String, String>,
    var minimum: Double,
    var status: ScorecardLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardLedgerStatus): Boolean {
        when (status) {
            ScorecardLedgerStatus.DRAFT -> {
                when (next) {
                    ScorecardLedgerStatus.SUSPENDED, ScorecardLedgerStatus.CLOSED -> return true
                }
                return false
            }
            ScorecardLedgerStatus.PENDING -> {
                when (next) {
                    ScorecardLedgerStatus.CLOSED -> return true
                }
                return false
            }
            ScorecardLedgerStatus.ACTIVE -> return next == ScorecardLedgerStatus.SUSPENDED || next == ScorecardLedgerStatus.CLOSED
            ScorecardLedgerStatus.SUSPENDED -> return next == ScorecardLedgerStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScorecardLedgerStatus) {
        if (!canMoveTo(next)) {
            error("scorecard ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round(floorValue * minimum * 100) / 100

    fun snapshot(): ScorecardLedger {
        return ScorecardLedger(
            code = code,
            title = title,
            limit = limit,
            floorValue = floorValue,
            category = category,
            quantity = quantity,
            contactEmail = contactEmail,
            attributes = attributes,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scorecard ledger in status draft. */
fun newScorecardLedger(code: String, title: String): ScorecardLedger {
    return ScorecardLedger(
        code = code,
        title = title,
        limit = 0,
        floorValue = 0.0,
        category = "",
        quantity = 0,
        contactEmail = "",
        attributes = mutableMapOf<String, String>(),
        minimum = 0.0,
        status = ScorecardLedgerStatus.DRAFT,
    )
}
