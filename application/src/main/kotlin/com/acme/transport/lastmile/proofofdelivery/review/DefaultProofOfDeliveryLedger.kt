package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default proof of delivery ledger is in its life cycle. */
enum class DefaultProofOfDeliveryLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a default proof of delivery ledger and its current status. */
data class DefaultProofOfDeliveryLedger(
    var code: String,
    var title: String,
    var routeCode: String,
    var limit: Int,
    var carrier: String,
    var trailerId: String,
    var pallets: Int,
    var reviewer: String,
    var margin: Double,
    var name: String,
    var status: DefaultProofOfDeliveryLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultProofOfDeliveryLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultProofOfDeliveryLedgerStatus): Boolean {
        when (status) {
            DefaultProofOfDeliveryLedgerStatus.IDLE -> {
                when (next) {
                    DefaultProofOfDeliveryLedgerStatus.WARMING, DefaultProofOfDeliveryLedgerStatus.DRAINING -> return true
                }
                return false
            }
            DefaultProofOfDeliveryLedgerStatus.WARMING -> return next == DefaultProofOfDeliveryLedgerStatus.STOPPED
            DefaultProofOfDeliveryLedgerStatus.READY -> {
                when (next) {
                    DefaultProofOfDeliveryLedgerStatus.DRAINING -> return true
                }
                return false
            }
            DefaultProofOfDeliveryLedgerStatus.DRAINING -> {
                when (next) {
                    DefaultProofOfDeliveryLedgerStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultProofOfDeliveryLedgerStatus) {
        if (!canMoveTo(next)) {
            error("default proof of delivery ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedExposure(): Double = round((limit.toDouble() + margin) * 100) / 100

    fun snapshot(): DefaultProofOfDeliveryLedger {
        return DefaultProofOfDeliveryLedger(
            code = code,
            title = title,
            routeCode = routeCode,
            limit = limit,
            carrier = carrier,
            trailerId = trailerId,
            pallets = pallets,
            reviewer = reviewer,
            margin = margin,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default proof of delivery ledger in status idle. */
fun newDefaultProofOfDeliveryLedger(code: String, title: String): DefaultProofOfDeliveryLedger {
    return DefaultProofOfDeliveryLedger(
        code = code,
        title = title,
        routeCode = "",
        limit = 0,
        carrier = "",
        trailerId = "",
        pallets = 0,
        reviewer = "",
        margin = 0.0,
        name = "",
        status = DefaultProofOfDeliveryLedgerStatus.IDLE,
    )
}
