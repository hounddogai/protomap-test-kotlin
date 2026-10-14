package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive lease return ledger is in its life cycle. */
enum class AdaptiveLeaseReturnLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A adaptive lease return ledger. Its code is unique within a tenant. */
data class AdaptiveLeaseReturnLedger(
    var code: String,
    var name: String,
    var capacity: Int,
    var category: String,
    var ownerId: String,
    var fuelLiters: Double,
    var status: AdaptiveLeaseReturnLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLeaseReturnLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLeaseReturnLedgerStatus): Boolean {
        when (status) {
            AdaptiveLeaseReturnLedgerStatus.CREATED -> {
                when (next) {
                    AdaptiveLeaseReturnLedgerStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveLeaseReturnLedgerStatus.VALIDATED -> {
                when (next) {
                    AdaptiveLeaseReturnLedgerStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveLeaseReturnLedgerStatus.PUBLISHED -> return next == AdaptiveLeaseReturnLedgerStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveLeaseReturnLedgerStatus) {
        if (!canMoveTo(next)) {
            error("adaptive lease return ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive lease return ledger"} $code"

    fun netVolume(): Double = round((fuelLiters + capacity.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveLeaseReturnLedger {
        return AdaptiveLeaseReturnLedger(
            code = code,
            name = name,
            capacity = capacity,
            category = category,
            ownerId = ownerId,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a adaptive lease return ledger in status created. */
fun newAdaptiveLeaseReturnLedger(code: String, name: String): AdaptiveLeaseReturnLedger {
    return AdaptiveLeaseReturnLedger(
        code = code,
        name = name,
        capacity = 0,
        category = "",
        ownerId = "",
        fuelLiters = 0.0,
        status = AdaptiveLeaseReturnLedgerStatus.CREATED,
    )
}
