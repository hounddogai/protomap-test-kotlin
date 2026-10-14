package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.text.normalize

// The pending redelivery window records of lastmile's redelivery work, and the rules, stores, and
// calculations around them.

/** Where a pending redelivery window is in its life cycle. */
enum class PendingRedeliveryWindowStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending redelivery window as the redelivery team keeps it. */
data class PendingRedeliveryWindow(
    var code: String,
    var name: String,
    var amount: Double,
    var carrier: String,
    var source: String,
    var status: PendingRedeliveryWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRedeliveryWindowStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRedeliveryWindowStatus): Boolean {
        when (status) {
            PendingRedeliveryWindowStatus.REQUESTED -> return next == PendingRedeliveryWindowStatus.GRANTED
            PendingRedeliveryWindowStatus.GRANTED -> {
                when (next) {
                    PendingRedeliveryWindowStatus.REVOKED, PendingRedeliveryWindowStatus.EXPIRED -> return true
                }
                return false
            }
            PendingRedeliveryWindowStatus.REVOKED -> return next == PendingRedeliveryWindowStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingRedeliveryWindowStatus) {
        if (!canMoveTo(next)) {
            error("pending redelivery window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = amount

    fun snapshot(): PendingRedeliveryWindow {
        return PendingRedeliveryWindow(
            code = code,
            name = name,
            amount = amount,
            carrier = carrier,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a pending redelivery window in status requested. */
fun newPendingRedeliveryWindow(code: String, name: String): PendingRedeliveryWindow {
    return PendingRedeliveryWindow(
        code = code,
        name = name,
        amount = 0.0,
        carrier = "",
        source = "",
        status = PendingRedeliveryWindowStatus.REQUESTED,
    )
}
