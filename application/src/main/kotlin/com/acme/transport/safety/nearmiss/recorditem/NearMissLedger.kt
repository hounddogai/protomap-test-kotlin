package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss ledger is in its life cycle. */
enum class NearMissLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One near miss ledger, with what the planners need to act on it. */
data class NearMissLedger(
    var code: String,
    var name: String,
    var loadKg: Double,
    var expiresAt: Int,
    var dock: String,
    var status: NearMissLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissLedgerStatus): Boolean {
        when (status) {
            NearMissLedgerStatus.PLANNED -> {
                when (next) {
                    NearMissLedgerStatus.IN_PROGRESS, NearMissLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            NearMissLedgerStatus.SCHEDULED -> {
                when (next) {
                    NearMissLedgerStatus.COMPLETED -> return true
                }
                return false
            }
            NearMissLedgerStatus.IN_PROGRESS -> {
                when (next) {
                    NearMissLedgerStatus.COMPLETED, NearMissLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            NearMissLedgerStatus.COMPLETED -> return next == NearMissLedgerStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: NearMissLedgerStatus) {
        if (!canMoveTo(next)) {
            error("near miss ledger $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"near miss ledger"} $code"

    fun weightedLoad(): Double = round((loadKg + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): NearMissLedger {
        return NearMissLedger(
            code = code,
            name = name,
            loadKg = loadKg,
            expiresAt = expiresAt,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a near miss ledger in status planned. */
fun newNearMissLedger(code: String, name: String): NearMissLedger {
    return NearMissLedger(
        code = code,
        name = name,
        loadKg = 0.0,
        expiresAt = 0,
        dock = "",
        status = NearMissLedgerStatus.PLANNED,
    )
}
