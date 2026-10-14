package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice agreement is in its life cycle. */
enum class FreightInvoiceAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A freight invoice agreement. Its code is unique within a tenant. */
data class FreightInvoiceAgreement(
    var code: String,
    var name: String,
    var floorValue: Double,
    var attempts: Int,
    var carrier: String,
    var category: String,
    var status: FreightInvoiceAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceAgreementStatus): Boolean {
        when (status) {
            FreightInvoiceAgreementStatus.CREATED -> {
                when (next) {
                    FreightInvoiceAgreementStatus.PUBLISHED, FreightInvoiceAgreementStatus.RETIRED -> return true
                }
                return false
            }
            FreightInvoiceAgreementStatus.VALIDATED -> {
                when (next) {
                    FreightInvoiceAgreementStatus.PUBLISHED -> return true
                }
                return false
            }
            FreightInvoiceAgreementStatus.PUBLISHED -> {
                when (next) {
                    FreightInvoiceAgreementStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceAgreementStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedValue(): Double = round((attempts.toDouble() - floorValue) * 100) / 100

    fun snapshot(): FreightInvoiceAgreement {
        return FreightInvoiceAgreement(
            code = code,
            name = name,
            floorValue = floorValue,
            attempts = attempts,
            carrier = carrier,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a freight invoice agreement in status created. */
fun newFreightInvoiceAgreement(code: String, name: String): FreightInvoiceAgreement {
    return FreightInvoiceAgreement(
        code = code,
        name = name,
        floorValue = 0.0,
        attempts = 0,
        carrier = "",
        category = "",
        status = FreightInvoiceAgreementStatus.CREATED,
    )
}
