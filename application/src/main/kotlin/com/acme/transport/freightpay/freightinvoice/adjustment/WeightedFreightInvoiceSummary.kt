package com.acme.transport.freightpay.freightinvoice.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted freight invoice summary is in its life cycle. */
enum class WeightedFreightInvoiceSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a weighted freight invoice summary and its current status. */
data class WeightedFreightInvoiceSummary(
    var code: String,
    var name: String,
    var retries: Int,
    var version: Int,
    var country: String,
    var visible: Boolean,
    var fuelLiters: Double,
    var status: WeightedFreightInvoiceSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFreightInvoiceSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFreightInvoiceSummaryStatus): Boolean {
        when (status) {
            WeightedFreightInvoiceSummaryStatus.PROPOSED -> return next == WeightedFreightInvoiceSummaryStatus.APPROVED
            WeightedFreightInvoiceSummaryStatus.APPROVED -> {
                when (next) {
                    WeightedFreightInvoiceSummaryStatus.REJECTED, WeightedFreightInvoiceSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedFreightInvoiceSummaryStatus.REJECTED -> return next == WeightedFreightInvoiceSummaryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: WeightedFreightInvoiceSummaryStatus) {
        if (!canMoveTo(next)) {
            error("weighted freight invoice summary $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"weighted freight invoice summary"} $code"

    fun projectedValue(): Double = round((fuelLiters + version.toDouble()) * 100) / 100

    fun snapshot(): WeightedFreightInvoiceSummary {
        return WeightedFreightInvoiceSummary(
            code = code,
            name = name,
            retries = retries,
            version = version,
            country = country,
            visible = visible,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a weighted freight invoice summary in status proposed. */
fun newWeightedFreightInvoiceSummary(code: String, name: String): WeightedFreightInvoiceSummary {
    return WeightedFreightInvoiceSummary(
        code = code,
        name = name,
        retries = 0,
        version = 0,
        country = "",
        visible = false,
        fuelLiters = 0.0,
        status = WeightedFreightInvoiceSummaryStatus.PROPOSED,
    )
}
