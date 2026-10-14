package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration ledger is in its life cycle. */
enum class DeclarationLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a declaration ledger and its current status. */
data class DeclarationLedger(
    var code: String,
    var title: String,
    var threshold: Double,
    var pallets: Int,
    var checksum: String,
    var driverId: String,
    var routeCode: String,
    var status: DeclarationLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationLedgerStatus): Boolean {
        when (status) {
            DeclarationLedgerStatus.REQUESTED -> {
                when (next) {
                    DeclarationLedgerStatus.GRANTED, DeclarationLedgerStatus.REVOKED -> return true
                }
                return false
            }
            DeclarationLedgerStatus.GRANTED -> {
                when (next) {
                    DeclarationLedgerStatus.REVOKED, DeclarationLedgerStatus.EXPIRED -> return true
                }
                return false
            }
            DeclarationLedgerStatus.REVOKED -> {
                when (next) {
                    DeclarationLedgerStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationLedgerStatus) {
        if (!canMoveTo(next)) {
            error("declaration ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"declaration ledger"} $code"

    fun projectedLoad(): Double = round((pallets.toDouble() + threshold) * 100) / 100

    fun snapshot(): DeclarationLedger {
        return DeclarationLedger(
            code = code,
            title = title,
            threshold = threshold,
            pallets = pallets,
            checksum = checksum,
            driverId = driverId,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a declaration ledger in status requested. */
fun newDeclarationLedger(code: String, title: String): DeclarationLedger {
    return DeclarationLedger(
        code = code,
        title = title,
        threshold = 0.0,
        pallets = 0,
        checksum = "",
        driverId = "",
        routeCode = "",
        status = DeclarationLedgerStatus.REQUESTED,
    )
}
