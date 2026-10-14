package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling idling ledger is in its life cycle. */
enum class RollingIdlingLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rolling idling ledger. Its code is unique within a tenant. */
data class RollingIdlingLedger(
    var code: String,
    var name: String,
    var phone: String,
    var maximum: Double,
    var description: String,
    var pinned: Boolean,
    var source: String,
    var durationMinutes: Int,
    var priority: Int,
    var endsAt: Int,
    var status: RollingIdlingLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingIdlingLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingIdlingLedgerStatus): Boolean {
        when (status) {
            RollingIdlingLedgerStatus.PROPOSED -> return next == RollingIdlingLedgerStatus.APPROVED || next == RollingIdlingLedgerStatus.WITHDRAWN
            RollingIdlingLedgerStatus.APPROVED -> return next == RollingIdlingLedgerStatus.REJECTED
            RollingIdlingLedgerStatus.REJECTED -> {
                when (next) {
                    RollingIdlingLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingIdlingLedgerStatus) {
        if (!canMoveTo(next)) {
            error("rolling idling ledger $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rolling idling ledger"} $code"

    fun netVolume(): Double = round((endsAt.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): RollingIdlingLedger {
        return RollingIdlingLedger(
            code = code,
            name = name,
            phone = phone,
            maximum = maximum,
            description = description,
            pinned = pinned,
            source = source,
            durationMinutes = durationMinutes,
            priority = priority,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rolling idling ledger in status proposed. */
fun newRollingIdlingLedger(code: String, name: String): RollingIdlingLedger {
    return RollingIdlingLedger(
        code = code,
        name = name,
        phone = "",
        maximum = 0.0,
        description = "",
        pinned = false,
        source = "",
        durationMinutes = 0,
        priority = 0,
        endsAt = 0,
        status = RollingIdlingLedgerStatus.PROPOSED,
    )
}
