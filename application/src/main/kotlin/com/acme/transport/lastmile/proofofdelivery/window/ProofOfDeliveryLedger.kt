package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery ledger is in its life cycle. */
enum class ProofOfDeliveryLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a proof of delivery ledger and its current status. */
data class ProofOfDeliveryLedger(
    var code: String,
    var title: String,
    var score: Double,
    var budget: Double,
    var createdAt: Int,
    var channel: String,
    var priority: Int,
    var version: Int,
    var status: ProofOfDeliveryLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryLedgerStatus): Boolean {
        when (status) {
            ProofOfDeliveryLedgerStatus.NEW -> {
                when (next) {
                    ProofOfDeliveryLedgerStatus.QUEUED, ProofOfDeliveryLedgerStatus.RUNNING -> return true
                }
                return false
            }
            ProofOfDeliveryLedgerStatus.QUEUED -> {
                when (next) {
                    ProofOfDeliveryLedgerStatus.DONE, ProofOfDeliveryLedgerStatus.FAILED -> return true
                }
                return false
            }
            ProofOfDeliveryLedgerStatus.RUNNING -> {
                when (next) {
                    ProofOfDeliveryLedgerStatus.DONE -> return true
                }
                return false
            }
            ProofOfDeliveryLedgerStatus.DONE -> return next == ProofOfDeliveryLedgerStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryLedgerStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery ledger $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"proof of delivery ledger"} $code"

    fun adjustedLoad(): Double = round((budget + priority.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliveryLedger {
        return ProofOfDeliveryLedger(
            code = code,
            title = title,
            score = score,
            budget = budget,
            createdAt = createdAt,
            channel = channel,
            priority = priority,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a proof of delivery ledger in status new. */
fun newProofOfDeliveryLedger(code: String, title: String): ProofOfDeliveryLedger {
    return ProofOfDeliveryLedger(
        code = code,
        title = title,
        score = 0.0,
        budget = 0.0,
        createdAt = 0,
        channel = "",
        priority = 0,
        version = 0,
        status = ProofOfDeliveryLedgerStatus.NEW,
    )
}
