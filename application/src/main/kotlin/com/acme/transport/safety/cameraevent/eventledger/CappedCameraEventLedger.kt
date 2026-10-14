package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped camera event ledger records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a capped camera event ledger is in its life cycle. */
enum class CappedCameraEventLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped camera event ledger as the camera event team keeps it. */
data class CappedCameraEventLedger(
    var code: String,
    var name: String,
    var batchSize: Int,
    var ratio: Double,
    var notes: String,
    var vehicleId: String,
    var expiresAt: Int,
    var minimum: Double,
    var attempts: Int,
    var threshold: Double,
    var status: CappedCameraEventLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCameraEventLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCameraEventLedgerStatus): Boolean {
        when (status) {
            CappedCameraEventLedgerStatus.NEW -> {
                when (next) {
                    CappedCameraEventLedgerStatus.RUNNING, CappedCameraEventLedgerStatus.DONE -> return true
                }
                return false
            }
            CappedCameraEventLedgerStatus.QUEUED -> return next == CappedCameraEventLedgerStatus.FAILED
            CappedCameraEventLedgerStatus.RUNNING -> {
                when (next) {
                    CappedCameraEventLedgerStatus.FAILED -> return true
                }
                return false
            }
            CappedCameraEventLedgerStatus.DONE -> {
                when (next) {
                    CappedCameraEventLedgerStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedCameraEventLedgerStatus) {
        if (!canMoveTo(next)) {
            error("capped camera event ledger $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netValue(): Double = round((attempts.toDouble() + ratio) * 100) / 100

    fun snapshot(): CappedCameraEventLedger {
        return CappedCameraEventLedger(
            code = code,
            name = name,
            batchSize = batchSize,
            ratio = ratio,
            notes = notes,
            vehicleId = vehicleId,
            expiresAt = expiresAt,
            minimum = minimum,
            attempts = attempts,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped camera event ledger in status new. */
fun newCappedCameraEventLedger(code: String, name: String): CappedCameraEventLedger {
    return CappedCameraEventLedger(
        code = code,
        name = name,
        batchSize = 0,
        ratio = 0.0,
        notes = "",
        vehicleId = "",
        expiresAt = 0,
        minimum = 0.0,
        attempts = 0,
        threshold = 0.0,
        status = CappedCameraEventLedgerStatus.NEW,
    )
}
