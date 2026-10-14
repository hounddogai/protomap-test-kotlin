package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary route offset ledger is in its life cycle. */
enum class PrimaryRouteOffsetLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary route offset ledger, with what the planners need to act on it. */
data class PrimaryRouteOffsetLedger(
    var code: String,
    var label: String,
    var lane: String,
    var score: Double,
    var startsAt: Int,
    var threshold: Double,
    var status: PrimaryRouteOffsetLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRouteOffsetLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRouteOffsetLedgerStatus): Boolean {
        when (status) {
            PrimaryRouteOffsetLedgerStatus.OPEN -> {
                when (next) {
                    PrimaryRouteOffsetLedgerStatus.RESOLVED, PrimaryRouteOffsetLedgerStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryRouteOffsetLedgerStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryRouteOffsetLedgerStatus.RESOLVED -> return true
                }
                return false
            }
            PrimaryRouteOffsetLedgerStatus.RESOLVED -> return next == PrimaryRouteOffsetLedgerStatus.REOPENED || next == PrimaryRouteOffsetLedgerStatus.ARCHIVED
            PrimaryRouteOffsetLedgerStatus.REOPENED -> {
                when (next) {
                    PrimaryRouteOffsetLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryRouteOffsetLedgerStatus) {
        if (!canMoveTo(next)) {
            error("primary route offset ledger $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveTotal(): Double = round((threshold + startsAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryRouteOffsetLedger {
        return PrimaryRouteOffsetLedger(
            code = code,
            label = label,
            lane = lane,
            score = score,
            startsAt = startsAt,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a primary route offset ledger in status open. */
fun newPrimaryRouteOffsetLedger(code: String, label: String): PrimaryRouteOffsetLedger {
    return PrimaryRouteOffsetLedger(
        code = code,
        label = label,
        lane = "",
        score = 0.0,
        startsAt = 0,
        threshold = 0.0,
        status = PrimaryRouteOffsetLedgerStatus.OPEN,
    )
}
