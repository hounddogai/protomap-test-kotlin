package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending freight invoice group is in its life cycle. */
enum class PendingFreightInvoiceGroupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a pending freight invoice group and its current status. */
data class PendingFreightInvoiceGroup(
    var code: String,
    var title: String,
    var region: String,
    var label: String,
    var source: String,
    var discount: Double,
    var distanceKm: Double,
    var attempts: Int,
    var threshold: Double,
    var channel: String,
    var timezone: String,
    var status: PendingFreightInvoiceGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingFreightInvoiceGroupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingFreightInvoiceGroupStatus): Boolean {
        when (status) {
            PendingFreightInvoiceGroupStatus.CREATED -> return next == PendingFreightInvoiceGroupStatus.VALIDATED || next == PendingFreightInvoiceGroupStatus.RETIRED
            PendingFreightInvoiceGroupStatus.VALIDATED -> return next == PendingFreightInvoiceGroupStatus.PUBLISHED || next == PendingFreightInvoiceGroupStatus.RETIRED
            PendingFreightInvoiceGroupStatus.PUBLISHED -> {
                when (next) {
                    PendingFreightInvoiceGroupStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingFreightInvoiceGroupStatus) {
        if (!canMoveTo(next)) {
            error("pending freight invoice group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending freight invoice group"} $code"

    fun netValue(): Double = round(distanceKm * threshold * 100) / 100

    fun snapshot(): PendingFreightInvoiceGroup {
        return PendingFreightInvoiceGroup(
            code = code,
            title = title,
            region = region,
            label = label,
            source = source,
            discount = discount,
            distanceKm = distanceKm,
            attempts = attempts,
            threshold = threshold,
            channel = channel,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending freight invoice group in status created. */
fun newPendingFreightInvoiceGroup(code: String, title: String): PendingFreightInvoiceGroup {
    return PendingFreightInvoiceGroup(
        code = code,
        title = title,
        region = "",
        label = "",
        source = "",
        discount = 0.0,
        distanceKm = 0.0,
        attempts = 0,
        threshold = 0.0,
        channel = "",
        timezone = "",
        status = PendingFreightInvoiceGroupStatus.CREATED,
    )
}
