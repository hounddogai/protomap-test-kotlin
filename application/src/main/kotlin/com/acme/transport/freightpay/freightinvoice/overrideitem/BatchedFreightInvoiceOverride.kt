package com.acme.transport.freightpay.freightinvoice.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched freight invoice override records of freightpay's freight invoice work, and the rules, stores,
// and calculations around them.

/** Where a batched freight invoice override is in its life cycle. */
enum class BatchedFreightInvoiceOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a batched freight invoice override and its current status. */
data class BatchedFreightInvoiceOverride(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var threshold: Double,
    var fuelLiters: Double,
    var startsAt: Int,
    var expiresAt: Int,
    var minimum: Double,
    var externalRef: String,
    var attributes: MutableMap<String, String>,
    var status: BatchedFreightInvoiceOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedFreightInvoiceOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedFreightInvoiceOverrideStatus): Boolean {
        when (status) {
            BatchedFreightInvoiceOverrideStatus.PLANNED -> return next == BatchedFreightInvoiceOverrideStatus.IN_PROGRESS
            BatchedFreightInvoiceOverrideStatus.SCHEDULED -> {
                when (next) {
                    BatchedFreightInvoiceOverrideStatus.COMPLETED, BatchedFreightInvoiceOverrideStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedFreightInvoiceOverrideStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedFreightInvoiceOverrideStatus.COMPLETED, BatchedFreightInvoiceOverrideStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedFreightInvoiceOverrideStatus.COMPLETED -> {
                when (next) {
                    BatchedFreightInvoiceOverrideStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedFreightInvoiceOverrideStatus) {
        if (!canMoveTo(next)) {
            error("batched freight invoice override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round((startsAt.toDouble() + minimum) * 100) / 100

    fun snapshot(): BatchedFreightInvoiceOverride {
        return BatchedFreightInvoiceOverride(
            code = code,
            label = label,
            pinned = pinned,
            threshold = threshold,
            fuelLiters = fuelLiters,
            startsAt = startsAt,
            expiresAt = expiresAt,
            minimum = minimum,
            externalRef = externalRef,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched freight invoice override in status planned. */
fun newBatchedFreightInvoiceOverride(code: String, label: String): BatchedFreightInvoiceOverride {
    return BatchedFreightInvoiceOverride(
        code = code,
        label = label,
        pinned = false,
        threshold = 0.0,
        fuelLiters = 0.0,
        startsAt = 0,
        expiresAt = 0,
        minimum = 0.0,
        externalRef = "",
        attributes = mutableMapOf<String, String>(),
        status = BatchedFreightInvoiceOverrideStatus.PLANNED,
    )
}
