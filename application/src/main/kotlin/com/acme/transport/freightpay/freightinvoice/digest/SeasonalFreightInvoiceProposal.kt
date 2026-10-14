package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal freight invoice proposal is in its life cycle. */
enum class SeasonalFreightInvoiceProposalStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One seasonal freight invoice proposal, with what the planners need to act on it. */
data class SeasonalFreightInvoiceProposal(
    var code: String,
    var label: String,
    var maximum: Double,
    var odometer: Int,
    var graceDays: Int,
    var batchSize: Int,
    var description: String,
    var active: Boolean,
    var dock: String,
    var etaMinutes: Int,
    var reviewer: String,
    var status: SeasonalFreightInvoiceProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalFreightInvoiceProposalStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalFreightInvoiceProposalStatus): Boolean {
        when (status) {
            SeasonalFreightInvoiceProposalStatus.DRAFT -> return next == SeasonalFreightInvoiceProposalStatus.SUSPENDED
            SeasonalFreightInvoiceProposalStatus.PENDING -> return next == SeasonalFreightInvoiceProposalStatus.CLOSED
            SeasonalFreightInvoiceProposalStatus.ACTIVE -> {
                when (next) {
                    SeasonalFreightInvoiceProposalStatus.SUSPENDED -> return true
                }
                return false
            }
            SeasonalFreightInvoiceProposalStatus.SUSPENDED -> {
                when (next) {
                    SeasonalFreightInvoiceProposalStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalFreightInvoiceProposalStatus) {
        if (!canMoveTo(next)) {
            error("seasonal freight invoice proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal freight invoice proposal"} $code"

    fun effectiveTotal(): Double = round((maximum + batchSize.toDouble()) * 100) / 100

    fun snapshot(): SeasonalFreightInvoiceProposal {
        return SeasonalFreightInvoiceProposal(
            code = code,
            label = label,
            maximum = maximum,
            odometer = odometer,
            graceDays = graceDays,
            batchSize = batchSize,
            description = description,
            active = active,
            dock = dock,
            etaMinutes = etaMinutes,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal freight invoice proposal in status draft. */
fun newSeasonalFreightInvoiceProposal(code: String, label: String): SeasonalFreightInvoiceProposal {
    return SeasonalFreightInvoiceProposal(
        code = code,
        label = label,
        maximum = 0.0,
        odometer = 0,
        graceDays = 0,
        batchSize = 0,
        description = "",
        active = false,
        dock = "",
        etaMinutes = 0,
        reviewer = "",
        status = SeasonalFreightInvoiceProposalStatus.DRAFT,
    )
}
