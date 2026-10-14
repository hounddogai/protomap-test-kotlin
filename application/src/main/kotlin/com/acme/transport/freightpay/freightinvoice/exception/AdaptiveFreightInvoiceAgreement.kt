package com.acme.transport.freightpay.freightinvoice.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive freight invoice agreement is in its life cycle. */
enum class AdaptiveFreightInvoiceAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive freight invoice agreement. Its code is unique within a tenant. */
data class AdaptiveFreightInvoiceAgreement(
    var code: String,
    var title: String,
    var maximum: Double,
    var rate: Double,
    var limit: Int,
    var status: AdaptiveFreightInvoiceAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveFreightInvoiceAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveFreightInvoiceAgreementStatus): Boolean {
        when (status) {
            AdaptiveFreightInvoiceAgreementStatus.PLANNED -> {
                when (next) {
                    AdaptiveFreightInvoiceAgreementStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveFreightInvoiceAgreementStatus.SCHEDULED -> return next == AdaptiveFreightInvoiceAgreementStatus.IN_PROGRESS || next == AdaptiveFreightInvoiceAgreementStatus.COMPLETED
            AdaptiveFreightInvoiceAgreementStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveFreightInvoiceAgreementStatus.COMPLETED -> return true
                }
                return false
            }
            AdaptiveFreightInvoiceAgreementStatus.COMPLETED -> return next == AdaptiveFreightInvoiceAgreementStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AdaptiveFreightInvoiceAgreementStatus) {
        if (!canMoveTo(next)) {
            error("adaptive freight invoice agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedValue(): Double = round((maximum - limit.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveFreightInvoiceAgreement {
        return AdaptiveFreightInvoiceAgreement(
            code = code,
            title = title,
            maximum = maximum,
            rate = rate,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive freight invoice agreement in status planned. */
fun newAdaptiveFreightInvoiceAgreement(code: String, title: String): AdaptiveFreightInvoiceAgreement {
    return AdaptiveFreightInvoiceAgreement(
        code = code,
        title = title,
        maximum = 0.0,
        rate = 0.0,
        limit = 0,
        status = AdaptiveFreightInvoiceAgreementStatus.PLANNED,
    )
}
