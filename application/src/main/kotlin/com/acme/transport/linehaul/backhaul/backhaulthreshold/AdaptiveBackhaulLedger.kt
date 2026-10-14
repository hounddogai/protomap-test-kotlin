package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive backhaul ledger is in its life cycle. */
enum class AdaptiveBackhaulLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a adaptive backhaul ledger and its current status. */
data class AdaptiveBackhaulLedger(
    var code: String,
    var title: String,
    var endsAt: Int,
    var carrier: String,
    var budget: Double,
    var category: String,
    var expiresAt: Int,
    var status: AdaptiveBackhaulLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveBackhaulLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveBackhaulLedgerStatus): Boolean {
        when (status) {
            AdaptiveBackhaulLedgerStatus.IDLE -> {
                when (next) {
                    AdaptiveBackhaulLedgerStatus.WARMING -> return true
                }
                return false
            }
            AdaptiveBackhaulLedgerStatus.WARMING -> {
                when (next) {
                    AdaptiveBackhaulLedgerStatus.READY, AdaptiveBackhaulLedgerStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveBackhaulLedgerStatus.READY -> {
                when (next) {
                    AdaptiveBackhaulLedgerStatus.DRAINING, AdaptiveBackhaulLedgerStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveBackhaulLedgerStatus.DRAINING -> {
                when (next) {
                    AdaptiveBackhaulLedgerStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveBackhaulLedgerStatus) {
        if (!canMoveTo(next)) {
            error("adaptive backhaul ledger $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive backhaul ledger"} $code"

    fun weightedTotal(): Double = round((expiresAt.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveBackhaulLedger {
        return AdaptiveBackhaulLedger(
            code = code,
            title = title,
            endsAt = endsAt,
            carrier = carrier,
            budget = budget,
            category = category,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a adaptive backhaul ledger in status idle. */
fun newAdaptiveBackhaulLedger(code: String, title: String): AdaptiveBackhaulLedger {
    return AdaptiveBackhaulLedger(
        code = code,
        title = title,
        endsAt = 0,
        carrier = "",
        budget = 0.0,
        category = "",
        expiresAt = 0,
        status = AdaptiveBackhaulLedgerStatus.IDLE,
    )
}
