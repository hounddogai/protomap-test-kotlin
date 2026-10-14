package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal freight invoice window is in its life cycle. */
enum class SeasonalFreightInvoiceWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal freight invoice window. Its code is unique within a tenant. */
data class SeasonalFreightInvoiceWindow(
    var code: String,
    var name: String,
    var budget: Double,
    var batchSize: Int,
    var limit: Int,
    var endsAt: Int,
    var status: SeasonalFreightInvoiceWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalFreightInvoiceWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalFreightInvoiceWindowStatus): Boolean {
        when (status) {
            SeasonalFreightInvoiceWindowStatus.OPEN -> return next == SeasonalFreightInvoiceWindowStatus.ARCHIVED
            SeasonalFreightInvoiceWindowStatus.IN_REVIEW -> return next == SeasonalFreightInvoiceWindowStatus.ARCHIVED
            SeasonalFreightInvoiceWindowStatus.RESOLVED -> return next == SeasonalFreightInvoiceWindowStatus.ARCHIVED
            SeasonalFreightInvoiceWindowStatus.REOPENED -> return next == SeasonalFreightInvoiceWindowStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalFreightInvoiceWindowStatus) {
        if (!canMoveTo(next)) {
            error("seasonal freight invoice window $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedTotal(): Double = round((batchSize.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): SeasonalFreightInvoiceWindow {
        return SeasonalFreightInvoiceWindow(
            code = code,
            name = name,
            budget = budget,
            batchSize = batchSize,
            limit = limit,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal freight invoice window in status open. */
fun newSeasonalFreightInvoiceWindow(code: String, name: String): SeasonalFreightInvoiceWindow {
    return SeasonalFreightInvoiceWindow(
        code = code,
        name = name,
        budget = 0.0,
        batchSize = 0,
        limit = 0,
        endsAt = 0,
        status = SeasonalFreightInvoiceWindowStatus.OPEN,
    )
}
