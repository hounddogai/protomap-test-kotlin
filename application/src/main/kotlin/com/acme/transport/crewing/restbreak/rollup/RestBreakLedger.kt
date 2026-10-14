package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break ledger is in its life cycle. */
enum class RestBreakLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rest break ledger as the rest break team keeps it. */
data class RestBreakLedger(
    var code: String,
    var title: String,
    var amount: Double,
    var region: String,
    var expiresAt: Int,
    var segment: String,
    var createdAt: Int,
    var endsAt: Int,
    var archived: Boolean,
    var windowDays: Int,
    var status: RestBreakLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakLedgerStatus): Boolean {
        when (status) {
            RestBreakLedgerStatus.PLANNED -> return next == RestBreakLedgerStatus.SCHEDULED || next == RestBreakLedgerStatus.CANCELLED
            RestBreakLedgerStatus.SCHEDULED -> {
                when (next) {
                    RestBreakLedgerStatus.IN_PROGRESS, RestBreakLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            RestBreakLedgerStatus.IN_PROGRESS -> return next == RestBreakLedgerStatus.COMPLETED || next == RestBreakLedgerStatus.CANCELLED
            RestBreakLedgerStatus.COMPLETED -> {
                when (next) {
                    RestBreakLedgerStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakLedgerStatus) {
        if (!canMoveTo(next)) {
            error("rest break ledger $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedExposure(): Double = round((createdAt.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): RestBreakLedger {
        return RestBreakLedger(
            code = code,
            title = title,
            amount = amount,
            region = region,
            expiresAt = expiresAt,
            segment = segment,
            createdAt = createdAt,
            endsAt = endsAt,
            archived = archived,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rest break ledger in status planned. */
fun newRestBreakLedger(code: String, title: String): RestBreakLedger {
    return RestBreakLedger(
        code = code,
        title = title,
        amount = 0.0,
        region = "",
        expiresAt = 0,
        segment = "",
        createdAt = 0,
        endsAt = 0,
        archived = false,
        windowDays = 0,
        status = RestBreakLedgerStatus.PLANNED,
    )
}
