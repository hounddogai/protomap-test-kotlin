package com.acme.transport.lastmile.redelivery.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery ledger is in its life cycle. */
enum class RedeliveryLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a redelivery ledger and its current status. */
data class RedeliveryLedger(
    var code: String,
    var title: String,
    var rate: Double,
    var etaMinutes: Int,
    var minimum: Double,
    var attributes: MutableMap<String, String>,
    var label: String,
    var summary: String,
    var status: RedeliveryLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryLedgerStatus): Boolean {
        when (status) {
            RedeliveryLedgerStatus.DRAFT -> return next == RedeliveryLedgerStatus.PENDING || next == RedeliveryLedgerStatus.SUSPENDED
            RedeliveryLedgerStatus.PENDING -> return next == RedeliveryLedgerStatus.SUSPENDED
            RedeliveryLedgerStatus.ACTIVE -> {
                when (next) {
                    RedeliveryLedgerStatus.SUSPENDED -> return true
                }
                return false
            }
            RedeliveryLedgerStatus.SUSPENDED -> {
                when (next) {
                    RedeliveryLedgerStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryLedgerStatus) {
        if (!canMoveTo(next)) {
            error("redelivery ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedExposure(): Double = round(rate * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): RedeliveryLedger {
        return RedeliveryLedger(
            code = code,
            title = title,
            rate = rate,
            etaMinutes = etaMinutes,
            minimum = minimum,
            attributes = attributes,
            label = label,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a redelivery ledger in status draft. */
fun newRedeliveryLedger(code: String, title: String): RedeliveryLedger {
    return RedeliveryLedger(
        code = code,
        title = title,
        rate = 0.0,
        etaMinutes = 0,
        minimum = 0.0,
        attributes = mutableMapOf<String, String>(),
        label = "",
        summary = "",
        status = RedeliveryLedgerStatus.DRAFT,
    )
}
