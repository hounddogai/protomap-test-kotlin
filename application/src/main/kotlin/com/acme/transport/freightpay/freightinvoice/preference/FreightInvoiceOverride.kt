package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice override is in its life cycle. */
enum class FreightInvoiceOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One freight invoice override, with what the planners need to act on it. */
data class FreightInvoiceOverride(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var pallets: Int,
    var visible: Boolean,
    var stopCount: Int,
    var country: String,
    var active: Boolean,
    var floorValue: Double,
    var batchSize: Int,
    var endsAt: Int,
    var status: FreightInvoiceOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceOverrideStatus): Boolean {
        when (status) {
            FreightInvoiceOverrideStatus.OPEN -> return next == FreightInvoiceOverrideStatus.IN_REVIEW || next == FreightInvoiceOverrideStatus.RESOLVED
            FreightInvoiceOverrideStatus.IN_REVIEW -> return next == FreightInvoiceOverrideStatus.RESOLVED || next == FreightInvoiceOverrideStatus.ARCHIVED
            FreightInvoiceOverrideStatus.RESOLVED -> return next == FreightInvoiceOverrideStatus.REOPENED
            FreightInvoiceOverrideStatus.REOPENED -> {
                when (next) {
                    FreightInvoiceOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceOverrideStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice override $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"freight invoice override"} $code"

    fun adjustedExposure(): Double = round((expiresAt.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): FreightInvoiceOverride {
        return FreightInvoiceOverride(
            code = code,
            title = title,
            expiresAt = expiresAt,
            pallets = pallets,
            visible = visible,
            stopCount = stopCount,
            country = country,
            active = active,
            floorValue = floorValue,
            batchSize = batchSize,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a freight invoice override in status open. */
fun newFreightInvoiceOverride(code: String, title: String): FreightInvoiceOverride {
    return FreightInvoiceOverride(
        code = code,
        title = title,
        expiresAt = 0,
        pallets = 0,
        visible = false,
        stopCount = 0,
        country = "",
        active = false,
        floorValue = 0.0,
        batchSize = 0,
        endsAt = 0,
        status = FreightInvoiceOverrideStatus.OPEN,
    )
}
