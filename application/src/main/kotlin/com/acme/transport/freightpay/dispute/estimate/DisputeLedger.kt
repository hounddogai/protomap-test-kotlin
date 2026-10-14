package com.acme.transport.freightpay.dispute.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute ledger is in its life cycle. */
enum class DisputeLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One dispute ledger, with what the planners need to act on it. */
data class DisputeLedger(
    var code: String,
    var title: String,
    var quantity: Int,
    var approved: Boolean,
    var batchSize: Int,
    var phone: String,
    var margin: Double,
    var graceDays: Int,
    var lane: String,
    var floorValue: Double,
    var maximum: Double,
    var status: DisputeLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeLedgerStatus): Boolean {
        when (status) {
            DisputeLedgerStatus.NEW -> {
                when (next) {
                    DisputeLedgerStatus.DONE, DisputeLedgerStatus.FAILED -> return true
                }
                return false
            }
            DisputeLedgerStatus.QUEUED -> return next == DisputeLedgerStatus.RUNNING
            DisputeLedgerStatus.RUNNING -> return next == DisputeLedgerStatus.DONE || next == DisputeLedgerStatus.FAILED
            DisputeLedgerStatus.DONE -> {
                when (next) {
                    DisputeLedgerStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeLedgerStatus) {
        if (!canMoveTo(next)) {
            error("dispute ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netExposure(): Double = round(batchSize.toDouble() * margin * 100) / 100

    fun snapshot(): DisputeLedger {
        return DisputeLedger(
            code = code,
            title = title,
            quantity = quantity,
            approved = approved,
            batchSize = batchSize,
            phone = phone,
            margin = margin,
            graceDays = graceDays,
            lane = lane,
            floorValue = floorValue,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a dispute ledger in status new. */
fun newDisputeLedger(code: String, title: String): DisputeLedger {
    return DisputeLedger(
        code = code,
        title = title,
        quantity = 0,
        approved = false,
        batchSize = 0,
        phone = "",
        margin = 0.0,
        graceDays = 0,
        lane = "",
        floorValue = 0.0,
        maximum = 0.0,
        status = DisputeLedgerStatus.NEW,
    )
}
