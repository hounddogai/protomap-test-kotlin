package com.acme.transport.freightpay.freightinvoice.group

import com.acme.transport.kit.text.normalize

// The freight invoice group records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a freight invoice group is in its life cycle. */
enum class FreightInvoiceGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a freight invoice group and its current status. */
data class FreightInvoiceGroup(
    var code: String,
    var label: String,
    var pallets: Int,
    var tags: MutableList<String>,
    var currency: String,
    var status: FreightInvoiceGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceGroupStatus): Boolean {
        when (status) {
            FreightInvoiceGroupStatus.REQUESTED -> {
                when (next) {
                    FreightInvoiceGroupStatus.REVOKED, FreightInvoiceGroupStatus.EXPIRED -> return true
                }
                return false
            }
            FreightInvoiceGroupStatus.GRANTED -> return next == FreightInvoiceGroupStatus.REVOKED
            FreightInvoiceGroupStatus.REVOKED -> {
                when (next) {
                    FreightInvoiceGroupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceGroupStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): FreightInvoiceGroup {
        return FreightInvoiceGroup(
            code = code,
            label = label,
            pallets = pallets,
            tags = tags,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a freight invoice group in status requested. */
fun newFreightInvoiceGroup(code: String, label: String): FreightInvoiceGroup {
    return FreightInvoiceGroup(
        code = code,
        label = label,
        pallets = 0,
        tags = mutableListOf<String>(),
        currency = "",
        status = FreightInvoiceGroupStatus.REQUESTED,
    )
}
