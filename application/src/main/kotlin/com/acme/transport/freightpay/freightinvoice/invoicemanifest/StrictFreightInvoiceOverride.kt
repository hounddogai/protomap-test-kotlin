package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict freight invoice override is in its life cycle. */
enum class StrictFreightInvoiceOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A strict freight invoice override as the freight invoice team keeps it. */
data class StrictFreightInvoiceOverride(
    var code: String,
    var name: String,
    var priority: Int,
    var active: Boolean,
    var odometer: Int,
    var rate: Double,
    var createdAt: Int,
    var visible: Boolean,
    var shiftCode: String,
    var amount: Double,
    var score: Double,
    var status: StrictFreightInvoiceOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictFreightInvoiceOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictFreightInvoiceOverrideStatus): Boolean {
        when (status) {
            StrictFreightInvoiceOverrideStatus.IDLE -> return next == StrictFreightInvoiceOverrideStatus.WARMING
            StrictFreightInvoiceOverrideStatus.WARMING -> return next == StrictFreightInvoiceOverrideStatus.DRAINING
            StrictFreightInvoiceOverrideStatus.READY -> {
                when (next) {
                    StrictFreightInvoiceOverrideStatus.DRAINING -> return true
                }
                return false
            }
            StrictFreightInvoiceOverrideStatus.DRAINING -> {
                when (next) {
                    StrictFreightInvoiceOverrideStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictFreightInvoiceOverrideStatus) {
        if (!canMoveTo(next)) {
            error("strict freight invoice override $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict freight invoice override"} $code"

    fun weightedValue(): Double = round(amount * rate * 100) / 100

    fun snapshot(): StrictFreightInvoiceOverride {
        return StrictFreightInvoiceOverride(
            code = code,
            name = name,
            priority = priority,
            active = active,
            odometer = odometer,
            rate = rate,
            createdAt = createdAt,
            visible = visible,
            shiftCode = shiftCode,
            amount = amount,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict freight invoice override in status idle. */
fun newStrictFreightInvoiceOverride(code: String, name: String): StrictFreightInvoiceOverride {
    return StrictFreightInvoiceOverride(
        code = code,
        name = name,
        priority = 0,
        active = false,
        odometer = 0,
        rate = 0.0,
        createdAt = 0,
        visible = false,
        shiftCode = "",
        amount = 0.0,
        score = 0.0,
        status = StrictFreightInvoiceOverrideStatus.IDLE,
    )
}
