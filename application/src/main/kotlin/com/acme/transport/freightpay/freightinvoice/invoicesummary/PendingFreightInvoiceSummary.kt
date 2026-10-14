package com.acme.transport.freightpay.freightinvoice.invoicesummary

import com.acme.transport.kit.text.normalize

// The pending freight invoice summary records of freightpay's freight invoice work, and the rules, stores,
// and calculations around them.

/** Where a pending freight invoice summary is in its life cycle. */
enum class PendingFreightInvoiceSummaryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A pending freight invoice summary as the freight invoice team keeps it. */
data class PendingFreightInvoiceSummary(
    var code: String,
    var label: String,
    var batchSize: Int,
    var name: String,
    var trailerId: String,
    var contactEmail: String,
    var title: String,
    var category: String,
    var ownerId: String,
    var attributes: MutableMap<String, String>,
    var tags: MutableList<String>,
    var status: PendingFreightInvoiceSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingFreightInvoiceSummaryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingFreightInvoiceSummaryStatus): Boolean {
        when (status) {
            PendingFreightInvoiceSummaryStatus.IDLE -> return next == PendingFreightInvoiceSummaryStatus.READY || next == PendingFreightInvoiceSummaryStatus.STOPPED
            PendingFreightInvoiceSummaryStatus.WARMING -> return next == PendingFreightInvoiceSummaryStatus.DRAINING
            PendingFreightInvoiceSummaryStatus.READY -> return next == PendingFreightInvoiceSummaryStatus.DRAINING || next == PendingFreightInvoiceSummaryStatus.STOPPED
            PendingFreightInvoiceSummaryStatus.DRAINING -> {
                when (next) {
                    PendingFreightInvoiceSummaryStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingFreightInvoiceSummaryStatus) {
        if (!canMoveTo(next)) {
            error("pending freight invoice summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): PendingFreightInvoiceSummary {
        return PendingFreightInvoiceSummary(
            code = code,
            label = label,
            batchSize = batchSize,
            name = name,
            trailerId = trailerId,
            contactEmail = contactEmail,
            title = title,
            category = category,
            ownerId = ownerId,
            attributes = attributes,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending freight invoice summary in status idle. */
fun newPendingFreightInvoiceSummary(code: String, label: String): PendingFreightInvoiceSummary {
    return PendingFreightInvoiceSummary(
        code = code,
        label = label,
        batchSize = 0,
        name = "",
        trailerId = "",
        contactEmail = "",
        title = "",
        category = "",
        ownerId = "",
        attributes = mutableMapOf<String, String>(),
        tags = mutableListOf<String>(),
        status = PendingFreightInvoiceSummaryStatus.IDLE,
    )
}
