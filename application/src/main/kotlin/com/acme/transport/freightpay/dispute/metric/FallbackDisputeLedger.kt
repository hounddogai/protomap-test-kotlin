package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback dispute ledger is in its life cycle. */
enum class FallbackDisputeLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fallback dispute ledger, with what the planners need to act on it. */
data class FallbackDisputeLedger(
    var code: String,
    var name: String,
    var loadKg: Double,
    var budget: Double,
    var channel: String,
    var durationMinutes: Int,
    var etaMinutes: Int,
    var endsAt: Int,
    var fuelLiters: Double,
    var margin: Double,
    var status: FallbackDisputeLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDisputeLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDisputeLedgerStatus): Boolean {
        when (status) {
            FallbackDisputeLedgerStatus.DRAFT -> {
                when (next) {
                    FallbackDisputeLedgerStatus.ACTIVE -> return true
                }
                return false
            }
            FallbackDisputeLedgerStatus.PENDING -> return next == FallbackDisputeLedgerStatus.ACTIVE
            FallbackDisputeLedgerStatus.ACTIVE -> return next == FallbackDisputeLedgerStatus.SUSPENDED
            FallbackDisputeLedgerStatus.SUSPENDED -> return next == FallbackDisputeLedgerStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FallbackDisputeLedgerStatus) {
        if (!canMoveTo(next)) {
            error("fallback dispute ledger $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback dispute ledger"} $code"

    fun adjustedLoad(): Double = round((margin - loadKg) * 100) / 100

    fun snapshot(): FallbackDisputeLedger {
        return FallbackDisputeLedger(
            code = code,
            name = name,
            loadKg = loadKg,
            budget = budget,
            channel = channel,
            durationMinutes = durationMinutes,
            etaMinutes = etaMinutes,
            endsAt = endsAt,
            fuelLiters = fuelLiters,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fallback dispute ledger in status draft. */
fun newFallbackDisputeLedger(code: String, name: String): FallbackDisputeLedger {
    return FallbackDisputeLedger(
        code = code,
        name = name,
        loadKg = 0.0,
        budget = 0.0,
        channel = "",
        durationMinutes = 0,
        etaMinutes = 0,
        endsAt = 0,
        fuelLiters = 0.0,
        margin = 0.0,
        status = FallbackDisputeLedgerStatus.DRAFT,
    )
}
