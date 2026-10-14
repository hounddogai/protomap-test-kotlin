package com.acme.transport.freightpay.freightinvoice.adjustment

import com.acme.transport.kit.text.normalize

// The freight invoice adjustment records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a freight invoice adjustment is in its life cycle. */
enum class FreightInvoiceAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A freight invoice adjustment. Its code is unique within a tenant. */
data class FreightInvoiceAdjustment(
    var code: String,
    var name: String,
    var category: String,
    var attempts: Int,
    var region: String,
    var ownerId: String,
    var routeCode: String,
    var channel: String,
    var status: FreightInvoiceAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceAdjustmentStatus): Boolean {
        when (status) {
            FreightInvoiceAdjustmentStatus.NEW -> return next == FreightInvoiceAdjustmentStatus.QUEUED
            FreightInvoiceAdjustmentStatus.QUEUED -> {
                when (next) {
                    FreightInvoiceAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            FreightInvoiceAdjustmentStatus.RUNNING -> {
                when (next) {
                    FreightInvoiceAdjustmentStatus.DONE, FreightInvoiceAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            FreightInvoiceAdjustmentStatus.DONE -> return next == FreightInvoiceAdjustmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FreightInvoiceAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): FreightInvoiceAdjustment {
        return FreightInvoiceAdjustment(
            code = code,
            name = name,
            category = category,
            attempts = attempts,
            region = region,
            ownerId = ownerId,
            routeCode = routeCode,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a freight invoice adjustment in status new. */
fun newFreightInvoiceAdjustment(code: String, name: String): FreightInvoiceAdjustment {
    return FreightInvoiceAdjustment(
        code = code,
        name = name,
        category = "",
        attempts = 0,
        region = "",
        ownerId = "",
        routeCode = "",
        channel = "",
        status = FreightInvoiceAdjustmentStatus.NEW,
    )
}
