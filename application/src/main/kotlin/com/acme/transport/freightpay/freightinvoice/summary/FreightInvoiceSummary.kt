package com.acme.transport.freightpay.freightinvoice.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The freight invoice summary records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a freight invoice summary is in its life cycle. */
enum class FreightInvoiceSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a freight invoice summary and its current status. */
data class FreightInvoiceSummary(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var createdAt: Int,
    var amount: Double,
    var batchSize: Int,
    var endsAt: Int,
    var threshold: Double,
    var trailerId: String,
    var status: FreightInvoiceSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceSummaryStatus): Boolean {
        when (status) {
            FreightInvoiceSummaryStatus.CREATED -> return next == FreightInvoiceSummaryStatus.VALIDATED
            FreightInvoiceSummaryStatus.VALIDATED -> return next == FreightInvoiceSummaryStatus.PUBLISHED
            FreightInvoiceSummaryStatus.PUBLISHED -> {
                when (next) {
                    FreightInvoiceSummaryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceSummaryStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice summary $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedTotal(): Double = round((threshold + amount) * 100) / 100

    fun snapshot(): FreightInvoiceSummary {
        return FreightInvoiceSummary(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            createdAt = createdAt,
            amount = amount,
            batchSize = batchSize,
            endsAt = endsAt,
            threshold = threshold,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a freight invoice summary in status created. */
fun newFreightInvoiceSummary(code: String, title: String): FreightInvoiceSummary {
    return FreightInvoiceSummary(
        code = code,
        title = title,
        fuelLiters = 0.0,
        createdAt = 0,
        amount = 0.0,
        batchSize = 0,
        endsAt = 0,
        threshold = 0.0,
        trailerId = "",
        status = FreightInvoiceSummaryStatus.CREATED,
    )
}
