package com.acme.transport.freightpay.freightinvoice.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The freight invoice allocation records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a freight invoice allocation is in its life cycle. */
enum class FreightInvoiceAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a freight invoice allocation and its current status. */
data class FreightInvoiceAllocation(
    var code: String,
    var name: String,
    var attempts: Int,
    var depot: String,
    var attributes: MutableMap<String, String>,
    var version: Int,
    var contactEmail: String,
    var rate: Double,
    var lane: String,
    var channel: String,
    var weight: Double,
    var status: FreightInvoiceAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceAllocationStatus): Boolean {
        when (status) {
            FreightInvoiceAllocationStatus.IDLE -> {
                when (next) {
                    FreightInvoiceAllocationStatus.WARMING, FreightInvoiceAllocationStatus.READY -> return true
                }
                return false
            }
            FreightInvoiceAllocationStatus.WARMING -> {
                when (next) {
                    FreightInvoiceAllocationStatus.DRAINING, FreightInvoiceAllocationStatus.STOPPED -> return true
                }
                return false
            }
            FreightInvoiceAllocationStatus.READY -> return next == FreightInvoiceAllocationStatus.DRAINING || next == FreightInvoiceAllocationStatus.STOPPED
            FreightInvoiceAllocationStatus.DRAINING -> return next == FreightInvoiceAllocationStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FreightInvoiceAllocationStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice allocation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round((rate + version.toDouble()) * 100) / 100

    fun snapshot(): FreightInvoiceAllocation {
        return FreightInvoiceAllocation(
            code = code,
            name = name,
            attempts = attempts,
            depot = depot,
            attributes = attributes,
            version = version,
            contactEmail = contactEmail,
            rate = rate,
            lane = lane,
            channel = channel,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a freight invoice allocation in status idle. */
fun newFreightInvoiceAllocation(code: String, name: String): FreightInvoiceAllocation {
    return FreightInvoiceAllocation(
        code = code,
        name = name,
        attempts = 0,
        depot = "",
        attributes = mutableMapOf<String, String>(),
        version = 0,
        contactEmail = "",
        rate = 0.0,
        lane = "",
        channel = "",
        weight = 0.0,
        status = FreightInvoiceAllocationStatus.IDLE,
    )
}
