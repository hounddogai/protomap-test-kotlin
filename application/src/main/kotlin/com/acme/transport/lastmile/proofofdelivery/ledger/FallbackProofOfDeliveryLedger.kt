package com.acme.transport.lastmile.proofofdelivery.ledger

import com.acme.transport.kit.text.normalize

// The fallback proof of delivery ledger records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a fallback proof of delivery ledger is in its life cycle. */
enum class FallbackProofOfDeliveryLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a fallback proof of delivery ledger and its current status. */
data class FallbackProofOfDeliveryLedger(
    var code: String,
    var title: String,
    var vehicleId: String,
    var contactEmail: String,
    var fuelLiters: Double,
    var active: Boolean,
    var status: FallbackProofOfDeliveryLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackProofOfDeliveryLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackProofOfDeliveryLedgerStatus): Boolean {
        when (status) {
            FallbackProofOfDeliveryLedgerStatus.NEW -> {
                when (next) {
                    FallbackProofOfDeliveryLedgerStatus.FAILED -> return true
                }
                return false
            }
            FallbackProofOfDeliveryLedgerStatus.QUEUED -> {
                when (next) {
                    FallbackProofOfDeliveryLedgerStatus.RUNNING -> return true
                }
                return false
            }
            FallbackProofOfDeliveryLedgerStatus.RUNNING -> {
                when (next) {
                    FallbackProofOfDeliveryLedgerStatus.FAILED -> return true
                }
                return false
            }
            FallbackProofOfDeliveryLedgerStatus.DONE -> return next == FallbackProofOfDeliveryLedgerStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackProofOfDeliveryLedgerStatus) {
        if (!canMoveTo(next)) {
            error("fallback proof of delivery ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback proof of delivery ledger"} $code"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): FallbackProofOfDeliveryLedger {
        return FallbackProofOfDeliveryLedger(
            code = code,
            title = title,
            vehicleId = vehicleId,
            contactEmail = contactEmail,
            fuelLiters = fuelLiters,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback proof of delivery ledger in status new. */
fun newFallbackProofOfDeliveryLedger(code: String, title: String): FallbackProofOfDeliveryLedger {
    return FallbackProofOfDeliveryLedger(
        code = code,
        title = title,
        vehicleId = "",
        contactEmail = "",
        fuelLiters = 0.0,
        active = false,
        status = FallbackProofOfDeliveryLedgerStatus.NEW,
    )
}
