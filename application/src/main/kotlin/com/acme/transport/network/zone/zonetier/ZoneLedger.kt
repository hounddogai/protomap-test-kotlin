package com.acme.transport.network.zone.zonetier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone ledger is in its life cycle. */
enum class ZoneLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a zone ledger and its current status. */
data class ZoneLedger(
    var code: String,
    var title: String,
    var attempts: Int,
    var archived: Boolean,
    var expiresAt: Int,
    var fuelLiters: Double,
    var pallets: Int,
    var batchSize: Int,
    var budget: Double,
    var stopCount: Int,
    var active: Boolean,
    var status: ZoneLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneLedgerStatus): Boolean {
        when (status) {
            ZoneLedgerStatus.REQUESTED -> return next == ZoneLedgerStatus.GRANTED || next == ZoneLedgerStatus.REVOKED
            ZoneLedgerStatus.GRANTED -> return next == ZoneLedgerStatus.REVOKED || next == ZoneLedgerStatus.EXPIRED
            ZoneLedgerStatus.REVOKED -> return next == ZoneLedgerStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ZoneLedgerStatus) {
        if (!canMoveTo(next)) {
            error("zone ledger $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"zone ledger"} $code"

    fun projectedExposure(): Double = round(stopCount.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): ZoneLedger {
        return ZoneLedger(
            code = code,
            title = title,
            attempts = attempts,
            archived = archived,
            expiresAt = expiresAt,
            fuelLiters = fuelLiters,
            pallets = pallets,
            batchSize = batchSize,
            budget = budget,
            stopCount = stopCount,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a zone ledger in status requested. */
fun newZoneLedger(code: String, title: String): ZoneLedger {
    return ZoneLedger(
        code = code,
        title = title,
        attempts = 0,
        archived = false,
        expiresAt = 0,
        fuelLiters = 0.0,
        pallets = 0,
        batchSize = 0,
        budget = 0.0,
        stopCount = 0,
        active = false,
        status = ZoneLedgerStatus.REQUESTED,
    )
}
