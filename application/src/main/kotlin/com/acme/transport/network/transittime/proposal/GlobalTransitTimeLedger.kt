package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.text.normalize

/** Where a global transit time ledger is in its life cycle. */
enum class GlobalTransitTimeLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global transit time ledger as the transit time team keeps it. */
data class GlobalTransitTimeLedger(
    var code: String,
    var name: String,
    var vehicleId: String,
    var depot: String,
    var active: Boolean,
    var lane: String,
    var routeCode: String,
    var expiresAt: Int,
    var archived: Boolean,
    var title: String,
    var status: GlobalTransitTimeLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTransitTimeLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTransitTimeLedgerStatus): Boolean {
        when (status) {
            GlobalTransitTimeLedgerStatus.PROPOSED -> return next == GlobalTransitTimeLedgerStatus.APPROVED || next == GlobalTransitTimeLedgerStatus.REJECTED
            GlobalTransitTimeLedgerStatus.APPROVED -> {
                when (next) {
                    GlobalTransitTimeLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalTransitTimeLedgerStatus.REJECTED -> return next == GlobalTransitTimeLedgerStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalTransitTimeLedgerStatus) {
        if (!canMoveTo(next)) {
            error("global transit time ledger $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): GlobalTransitTimeLedger {
        return GlobalTransitTimeLedger(
            code = code,
            name = name,
            vehicleId = vehicleId,
            depot = depot,
            active = active,
            lane = lane,
            routeCode = routeCode,
            expiresAt = expiresAt,
            archived = archived,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global transit time ledger in status proposed. */
fun newGlobalTransitTimeLedger(code: String, name: String): GlobalTransitTimeLedger {
    return GlobalTransitTimeLedger(
        code = code,
        name = name,
        vehicleId = "",
        depot = "",
        active = false,
        lane = "",
        routeCode = "",
        expiresAt = 0,
        archived = false,
        title = "",
        status = GlobalTransitTimeLedgerStatus.PROPOSED,
    )
}
