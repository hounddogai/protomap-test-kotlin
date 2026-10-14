package com.acme.transport.freightpay.freightinvoice.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice audit is in its life cycle. */
enum class FreightInvoiceAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a freight invoice audit and its current status. */
data class FreightInvoiceAudit(
    var code: String,
    var name: String,
    var version: Int,
    var amount: Double,
    var source: String,
    var limit: Int,
    var pinned: Boolean,
    var contactEmail: String,
    var archived: Boolean,
    var status: FreightInvoiceAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceAuditStatus): Boolean {
        when (status) {
            FreightInvoiceAuditStatus.PLANNED -> return next == FreightInvoiceAuditStatus.IN_PROGRESS
            FreightInvoiceAuditStatus.SCHEDULED -> return next == FreightInvoiceAuditStatus.COMPLETED
            FreightInvoiceAuditStatus.IN_PROGRESS -> {
                when (next) {
                    FreightInvoiceAuditStatus.COMPLETED, FreightInvoiceAuditStatus.CANCELLED -> return true
                }
                return false
            }
            FreightInvoiceAuditStatus.COMPLETED -> {
                when (next) {
                    FreightInvoiceAuditStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceAuditStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice audit $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"freight invoice audit"} $code"

    fun adjustedLoad(): Double = round((limit.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): FreightInvoiceAudit {
        return FreightInvoiceAudit(
            code = code,
            name = name,
            version = version,
            amount = amount,
            source = source,
            limit = limit,
            pinned = pinned,
            contactEmail = contactEmail,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a freight invoice audit in status planned. */
fun newFreightInvoiceAudit(code: String, name: String): FreightInvoiceAudit {
    return FreightInvoiceAudit(
        code = code,
        name = name,
        version = 0,
        amount = 0.0,
        source = "",
        limit = 0,
        pinned = false,
        contactEmail = "",
        archived = false,
        status = FreightInvoiceAuditStatus.PLANNED,
    )
}
