package com.acme.transport.network.zone.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual zone ledger is in its life cycle. */
enum class ManualZoneLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual zone ledger as the zone team keeps it. */
data class ManualZoneLedger(
    var code: String,
    var label: String,
    var quantity: Int,
    var createdAt: Int,
    var fuelLiters: Double,
    var startsAt: Int,
    var status: ManualZoneLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualZoneLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualZoneLedgerStatus): Boolean {
        when (status) {
            ManualZoneLedgerStatus.DRAFT -> {
                when (next) {
                    ManualZoneLedgerStatus.SUSPENDED, ManualZoneLedgerStatus.CLOSED -> return true
                }
                return false
            }
            ManualZoneLedgerStatus.PENDING -> {
                when (next) {
                    ManualZoneLedgerStatus.ACTIVE -> return true
                }
                return false
            }
            ManualZoneLedgerStatus.ACTIVE -> return next == ManualZoneLedgerStatus.SUSPENDED
            ManualZoneLedgerStatus.SUSPENDED -> return next == ManualZoneLedgerStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ManualZoneLedgerStatus) {
        if (!canMoveTo(next)) {
            error("manual zone ledger $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedTotal(): Double = round(fuelLiters * createdAt.toDouble() * 100) / 100

    fun snapshot(): ManualZoneLedger {
        return ManualZoneLedger(
            code = code,
            label = label,
            quantity = quantity,
            createdAt = createdAt,
            fuelLiters = fuelLiters,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual zone ledger in status draft. */
fun newManualZoneLedger(code: String, label: String): ManualZoneLedger {
    return ManualZoneLedger(
        code = code,
        label = label,
        quantity = 0,
        createdAt = 0,
        fuelLiters = 0.0,
        startsAt = 0,
        status = ManualZoneLedgerStatus.DRAFT,
    )
}
