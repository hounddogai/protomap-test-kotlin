package com.acme.transport.crewing.roster.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster batch is in its life cycle. */
enum class RosterBatchStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A roster batch. Its code is unique within a tenant. */
data class RosterBatch(
    var code: String,
    var name: String,
    var loadKg: Double,
    var updatedAt: Int,
    var cost: Double,
    var windowDays: Int,
    var retries: Int,
    var carrier: String,
    var status: RosterBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterBatchStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterBatchStatus): Boolean {
        when (status) {
            RosterBatchStatus.PROPOSED -> return next == RosterBatchStatus.REJECTED || next == RosterBatchStatus.WITHDRAWN
            RosterBatchStatus.APPROVED -> return next == RosterBatchStatus.REJECTED || next == RosterBatchStatus.WITHDRAWN
            RosterBatchStatus.REJECTED -> {
                when (next) {
                    RosterBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterBatchStatus) {
        if (!canMoveTo(next)) {
            error("roster batch $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"roster batch"} $code"

    fun projectedTotal(): Double = round((loadKg - retries.toDouble()) * 100) / 100

    fun snapshot(): RosterBatch {
        return RosterBatch(
            code = code,
            name = name,
            loadKg = loadKg,
            updatedAt = updatedAt,
            cost = cost,
            windowDays = windowDays,
            retries = retries,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a roster batch in status proposed. */
fun newRosterBatch(code: String, name: String): RosterBatch {
    return RosterBatch(
        code = code,
        name = name,
        loadKg = 0.0,
        updatedAt = 0,
        cost = 0.0,
        windowDays = 0,
        retries = 0,
        carrier = "",
        status = RosterBatchStatus.PROPOSED,
    )
}
