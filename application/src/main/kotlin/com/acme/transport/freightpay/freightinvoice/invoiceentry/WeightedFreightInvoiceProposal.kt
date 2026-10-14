package com.acme.transport.freightpay.freightinvoice.invoiceentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted freight invoice proposal is in its life cycle. */
enum class WeightedFreightInvoiceProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a weighted freight invoice proposal and its current status. */
data class WeightedFreightInvoiceProposal(
    var code: String,
    var title: String,
    var routeCode: String,
    var pallets: Int,
    var checksum: String,
    var quantity: Int,
    var rate: Double,
    var version: Int,
    var channel: String,
    var windowDays: Int,
    var status: WeightedFreightInvoiceProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFreightInvoiceProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFreightInvoiceProposalStatus): Boolean {
        when (status) {
            WeightedFreightInvoiceProposalStatus.REQUESTED -> return next == WeightedFreightInvoiceProposalStatus.GRANTED || next == WeightedFreightInvoiceProposalStatus.REVOKED
            WeightedFreightInvoiceProposalStatus.GRANTED -> return next == WeightedFreightInvoiceProposalStatus.REVOKED
            WeightedFreightInvoiceProposalStatus.REVOKED -> return next == WeightedFreightInvoiceProposalStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: WeightedFreightInvoiceProposalStatus) {
        if (!canMoveTo(next)) {
            error("weighted freight invoice proposal $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun effectiveExposure(): Double = round((pallets.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): WeightedFreightInvoiceProposal {
        return WeightedFreightInvoiceProposal(
            code = code,
            title = title,
            routeCode = routeCode,
            pallets = pallets,
            checksum = checksum,
            quantity = quantity,
            rate = rate,
            version = version,
            channel = channel,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted freight invoice proposal in status requested. */
fun newWeightedFreightInvoiceProposal(code: String, title: String): WeightedFreightInvoiceProposal {
    return WeightedFreightInvoiceProposal(
        code = code,
        title = title,
        routeCode = "",
        pallets = 0,
        checksum = "",
        quantity = 0,
        rate = 0.0,
        version = 0,
        channel = "",
        windowDays = 0,
        status = WeightedFreightInvoiceProposalStatus.REQUESTED,
    )
}
