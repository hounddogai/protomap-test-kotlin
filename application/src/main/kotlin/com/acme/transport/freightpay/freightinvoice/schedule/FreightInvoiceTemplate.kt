package com.acme.transport.freightpay.freightinvoice.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice template is in its life cycle. */
enum class FreightInvoiceTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A freight invoice template. Its code is unique within a tenant. */
data class FreightInvoiceTemplate(
    var code: String,
    var title: String,
    var sequence: Int,
    var name: String,
    var reviewer: String,
    var ownerId: String,
    var depot: String,
    var capacity: Int,
    var status: FreightInvoiceTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceTemplateStatus): Boolean {
        when (status) {
            FreightInvoiceTemplateStatus.IDLE -> return next == FreightInvoiceTemplateStatus.READY || next == FreightInvoiceTemplateStatus.DRAINING
            FreightInvoiceTemplateStatus.WARMING -> {
                when (next) {
                    FreightInvoiceTemplateStatus.READY, FreightInvoiceTemplateStatus.STOPPED -> return true
                }
                return false
            }
            FreightInvoiceTemplateStatus.READY -> {
                when (next) {
                    FreightInvoiceTemplateStatus.DRAINING -> return true
                }
                return false
            }
            FreightInvoiceTemplateStatus.DRAINING -> return next == FreightInvoiceTemplateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FreightInvoiceTemplateStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"freight invoice template"} $code"

    fun netLoad(): Double = round((sequence.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): FreightInvoiceTemplate {
        return FreightInvoiceTemplate(
            code = code,
            title = title,
            sequence = sequence,
            name = name,
            reviewer = reviewer,
            ownerId = ownerId,
            depot = depot,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a freight invoice template in status idle. */
fun newFreightInvoiceTemplate(code: String, title: String): FreightInvoiceTemplate {
    return FreightInvoiceTemplate(
        code = code,
        title = title,
        sequence = 0,
        name = "",
        reviewer = "",
        ownerId = "",
        depot = "",
        capacity = 0,
        status = FreightInvoiceTemplateStatus.IDLE,
    )
}
