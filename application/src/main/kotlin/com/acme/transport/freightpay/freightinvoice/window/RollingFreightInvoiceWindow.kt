package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling freight invoice window records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a rolling freight invoice window is in its life cycle. */
enum class RollingFreightInvoiceWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a rolling freight invoice window and its current status. */
data class RollingFreightInvoiceWindow(
    var code: String,
    var label: String,
    var maximum: Double,
    var threshold: Double,
    var durationMinutes: Int,
    var loadKg: Double,
    var status: RollingFreightInvoiceWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFreightInvoiceWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFreightInvoiceWindowStatus): Boolean {
        when (status) {
            RollingFreightInvoiceWindowStatus.NEW -> return next == RollingFreightInvoiceWindowStatus.DONE
            RollingFreightInvoiceWindowStatus.QUEUED -> return next == RollingFreightInvoiceWindowStatus.RUNNING
            RollingFreightInvoiceWindowStatus.RUNNING -> return next == RollingFreightInvoiceWindowStatus.FAILED
            RollingFreightInvoiceWindowStatus.DONE -> return next == RollingFreightInvoiceWindowStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingFreightInvoiceWindowStatus) {
        if (!canMoveTo(next)) {
            error("rolling freight invoice window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedVolume(): Double = round(loadKg * maximum * 100) / 100

    fun snapshot(): RollingFreightInvoiceWindow {
        return RollingFreightInvoiceWindow(
            code = code,
            label = label,
            maximum = maximum,
            threshold = threshold,
            durationMinutes = durationMinutes,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling freight invoice window in status new. */
fun newRollingFreightInvoiceWindow(code: String, label: String): RollingFreightInvoiceWindow {
    return RollingFreightInvoiceWindow(
        code = code,
        label = label,
        maximum = 0.0,
        threshold = 0.0,
        durationMinutes = 0,
        loadKg = 0.0,
        status = RollingFreightInvoiceWindowStatus.NEW,
    )
}
