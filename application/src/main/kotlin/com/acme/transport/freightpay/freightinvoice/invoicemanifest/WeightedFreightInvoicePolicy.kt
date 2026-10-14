package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted freight invoice policy is in its life cycle. */
enum class WeightedFreightInvoicePolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One weighted freight invoice policy, with what the planners need to act on it. */
data class WeightedFreightInvoicePolicy(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var expiresAt: Int,
    var odometer: Int,
    var updatedAt: Int,
    var dock: String,
    var quantity: Int,
    var status: WeightedFreightInvoicePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFreightInvoicePolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFreightInvoicePolicyStatus): Boolean {
        when (status) {
            WeightedFreightInvoicePolicyStatus.NEW -> return next == WeightedFreightInvoicePolicyStatus.QUEUED
            WeightedFreightInvoicePolicyStatus.QUEUED -> return next == WeightedFreightInvoicePolicyStatus.RUNNING
            WeightedFreightInvoicePolicyStatus.RUNNING -> return next == WeightedFreightInvoicePolicyStatus.DONE || next == WeightedFreightInvoicePolicyStatus.FAILED
            WeightedFreightInvoicePolicyStatus.DONE -> {
                when (next) {
                    WeightedFreightInvoicePolicyStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedFreightInvoicePolicyStatus) {
        if (!canMoveTo(next)) {
            error("weighted freight invoice policy $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedExposure(): Double = round((updatedAt.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): WeightedFreightInvoicePolicy {
        return WeightedFreightInvoicePolicy(
            code = code,
            label = label,
            pinned = pinned,
            expiresAt = expiresAt,
            odometer = odometer,
            updatedAt = updatedAt,
            dock = dock,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted freight invoice policy in status new. */
fun newWeightedFreightInvoicePolicy(code: String, label: String): WeightedFreightInvoicePolicy {
    return WeightedFreightInvoicePolicy(
        code = code,
        label = label,
        pinned = false,
        expiresAt = 0,
        odometer = 0,
        updatedAt = 0,
        dock = "",
        quantity = 0,
        status = WeightedFreightInvoicePolicyStatus.NEW,
    )
}
