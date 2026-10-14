package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice target is in its life cycle. */
enum class FreightInvoiceTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A freight invoice target as the freight invoice team keeps it. */
data class FreightInvoiceTarget(
    var code: String,
    var title: String,
    var attempts: Int,
    var amount: Double,
    var createdAt: Int,
    var archived: Boolean,
    var weight: Double,
    var description: String,
    var discount: Double,
    var capacity: Int,
    var status: FreightInvoiceTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceTargetStatus): Boolean {
        when (status) {
            FreightInvoiceTargetStatus.CREATED -> {
                when (next) {
                    FreightInvoiceTargetStatus.VALIDATED, FreightInvoiceTargetStatus.PUBLISHED -> return true
                }
                return false
            }
            FreightInvoiceTargetStatus.VALIDATED -> {
                when (next) {
                    FreightInvoiceTargetStatus.PUBLISHED, FreightInvoiceTargetStatus.RETIRED -> return true
                }
                return false
            }
            FreightInvoiceTargetStatus.PUBLISHED -> return next == FreightInvoiceTargetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FreightInvoiceTargetStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice target $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"freight invoice target"} $code"

    fun projectedExposure(): Double = round((discount - amount) * 100) / 100

    fun snapshot(): FreightInvoiceTarget {
        return FreightInvoiceTarget(
            code = code,
            title = title,
            attempts = attempts,
            amount = amount,
            createdAt = createdAt,
            archived = archived,
            weight = weight,
            description = description,
            discount = discount,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a freight invoice target in status created. */
fun newFreightInvoiceTarget(code: String, title: String): FreightInvoiceTarget {
    return FreightInvoiceTarget(
        code = code,
        title = title,
        attempts = 0,
        amount = 0.0,
        createdAt = 0,
        archived = false,
        weight = 0.0,
        description = "",
        discount = 0.0,
        capacity = 0,
        status = FreightInvoiceTargetStatus.CREATED,
    )
}
