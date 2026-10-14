package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop ledger is in its life cycle. */
enum class DropLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A drop ledger. Its code is unique within a tenant. */
data class DropLedger(
    var code: String,
    var name: String,
    var segment: String,
    var timezone: String,
    var sequence: Int,
    var lane: String,
    var startsAt: Int,
    var discount: Double,
    var pallets: Int,
    var status: DropLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropLedgerStatus): Boolean {
        when (status) {
            DropLedgerStatus.DRAFT -> {
                when (next) {
                    DropLedgerStatus.ACTIVE -> return true
                }
                return false
            }
            DropLedgerStatus.PENDING -> {
                when (next) {
                    DropLedgerStatus.ACTIVE -> return true
                }
                return false
            }
            DropLedgerStatus.ACTIVE -> {
                when (next) {
                    DropLedgerStatus.CLOSED -> return true
                }
                return false
            }
            DropLedgerStatus.SUSPENDED -> {
                when (next) {
                    DropLedgerStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropLedgerStatus) {
        if (!canMoveTo(next)) {
            error("drop ledger $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedTotal(): Double = round(discount * pallets.toDouble() * 100) / 100

    fun snapshot(): DropLedger {
        return DropLedger(
            code = code,
            name = name,
            segment = segment,
            timezone = timezone,
            sequence = sequence,
            lane = lane,
            startsAt = startsAt,
            discount = discount,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a drop ledger in status draft. */
fun newDropLedger(code: String, name: String): DropLedger {
    return DropLedger(
        code = code,
        name = name,
        segment = "",
        timezone = "",
        sequence = 0,
        lane = "",
        startsAt = 0,
        discount = 0.0,
        pallets = 0,
        status = DropLedgerStatus.DRAFT,
    )
}
