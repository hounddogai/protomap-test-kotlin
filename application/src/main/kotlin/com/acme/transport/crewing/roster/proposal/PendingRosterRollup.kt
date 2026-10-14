package com.acme.transport.crewing.roster.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending roster rollup is in its life cycle. */
enum class PendingRosterRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A pending roster rollup as the roster team keeps it. */
data class PendingRosterRollup(
    var code: String,
    var title: String,
    var depot: String,
    var stopCount: Int,
    var active: Boolean,
    var driverId: String,
    var ownerId: String,
    var attributes: MutableMap<String, String>,
    var amount: Double,
    var status: PendingRosterRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRosterRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRosterRollupStatus): Boolean {
        when (status) {
            PendingRosterRollupStatus.NEW -> return next == PendingRosterRollupStatus.DONE || next == PendingRosterRollupStatus.FAILED
            PendingRosterRollupStatus.QUEUED -> return next == PendingRosterRollupStatus.RUNNING
            PendingRosterRollupStatus.RUNNING -> {
                when (next) {
                    PendingRosterRollupStatus.DONE, PendingRosterRollupStatus.FAILED -> return true
                }
                return false
            }
            PendingRosterRollupStatus.DONE -> return next == PendingRosterRollupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PendingRosterRollupStatus) {
        if (!canMoveTo(next)) {
            error("pending roster rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending roster rollup"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round((stopCount.toDouble() - amount) * 100) / 100

    fun snapshot(): PendingRosterRollup {
        return PendingRosterRollup(
            code = code,
            title = title,
            depot = depot,
            stopCount = stopCount,
            active = active,
            driverId = driverId,
            ownerId = ownerId,
            attributes = attributes,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a pending roster rollup in status new. */
fun newPendingRosterRollup(code: String, title: String): PendingRosterRollup {
    return PendingRosterRollup(
        code = code,
        title = title,
        depot = "",
        stopCount = 0,
        active = false,
        driverId = "",
        ownerId = "",
        attributes = mutableMapOf<String, String>(),
        amount = 0.0,
        status = PendingRosterRollupStatus.NEW,
    )
}
