package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback service plan ledger records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a fallback service plan ledger is in its life cycle. */
enum class FallbackServicePlanLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One fallback service plan ledger, with what the planners need to act on it. */
data class FallbackServicePlanLedger(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var unit: String,
    var loadKg: Double,
    var startsAt: Int,
    var maximum: Double,
    var tags: MutableList<String>,
    var windowDays: Int,
    var pallets: Int,
    var status: FallbackServicePlanLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackServicePlanLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackServicePlanLedgerStatus): Boolean {
        when (status) {
            FallbackServicePlanLedgerStatus.PROPOSED -> return next == FallbackServicePlanLedgerStatus.WITHDRAWN
            FallbackServicePlanLedgerStatus.APPROVED -> {
                when (next) {
                    FallbackServicePlanLedgerStatus.REJECTED, FallbackServicePlanLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackServicePlanLedgerStatus.REJECTED -> {
                when (next) {
                    FallbackServicePlanLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackServicePlanLedgerStatus) {
        if (!canMoveTo(next)) {
            error("fallback service plan ledger $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback service plan ledger"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round(pallets.toDouble() * maximum * 100) / 100

    fun snapshot(): FallbackServicePlanLedger {
        return FallbackServicePlanLedger(
            code = code,
            title = title,
            pinned = pinned,
            unit = unit,
            loadKg = loadKg,
            startsAt = startsAt,
            maximum = maximum,
            tags = tags,
            windowDays = windowDays,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback service plan ledger in status proposed. */
fun newFallbackServicePlanLedger(code: String, title: String): FallbackServicePlanLedger {
    return FallbackServicePlanLedger(
        code = code,
        title = title,
        pinned = false,
        unit = "",
        loadKg = 0.0,
        startsAt = 0,
        maximum = 0.0,
        tags = mutableListOf<String>(),
        windowDays = 0,
        pallets = 0,
        status = FallbackServicePlanLedgerStatus.PROPOSED,
    )
}
