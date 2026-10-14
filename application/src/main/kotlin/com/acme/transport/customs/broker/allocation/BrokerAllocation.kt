package com.acme.transport.customs.broker.allocation

import com.acme.transport.kit.text.normalize

// The broker allocation records of customs's broker work, and the rules, stores, and calculations around
// them.

/** Where a broker allocation is in its life cycle. */
enum class BrokerAllocationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A broker allocation as the broker team keeps it. */
data class BrokerAllocation(
    var code: String,
    var title: String,
    var name: String,
    var driverId: String,
    var segment: String,
    var amount: Double,
    var status: BrokerAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerAllocationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerAllocationStatus): Boolean {
        when (status) {
            BrokerAllocationStatus.NEW -> {
                when (next) {
                    BrokerAllocationStatus.QUEUED, BrokerAllocationStatus.DONE -> return true
                }
                return false
            }
            BrokerAllocationStatus.QUEUED -> return next == BrokerAllocationStatus.DONE || next == BrokerAllocationStatus.FAILED
            BrokerAllocationStatus.RUNNING -> return next == BrokerAllocationStatus.DONE || next == BrokerAllocationStatus.FAILED
            BrokerAllocationStatus.DONE -> return next == BrokerAllocationStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BrokerAllocationStatus) {
        if (!canMoveTo(next)) {
            error("broker allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = amount

    fun snapshot(): BrokerAllocation {
        return BrokerAllocation(
            code = code,
            title = title,
            name = name,
            driverId = driverId,
            segment = segment,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a broker allocation in status new. */
fun newBrokerAllocation(code: String, title: String): BrokerAllocation {
    return BrokerAllocation(
        code = code,
        title = title,
        name = "",
        driverId = "",
        segment = "",
        amount = 0.0,
        status = BrokerAllocationStatus.NEW,
    )
}
