package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice baseline is in its life cycle. */
enum class FreightInvoiceBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a freight invoice baseline and its current status. */
data class FreightInvoiceBaseline(
    var code: String,
    var label: String,
    var checksum: String,
    var endsAt: Int,
    var version: Int,
    var status: FreightInvoiceBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceBaselineStatus): Boolean {
        when (status) {
            FreightInvoiceBaselineStatus.IDLE -> return next == FreightInvoiceBaselineStatus.READY || next == FreightInvoiceBaselineStatus.DRAINING
            FreightInvoiceBaselineStatus.WARMING -> return next == FreightInvoiceBaselineStatus.READY
            FreightInvoiceBaselineStatus.READY -> return next == FreightInvoiceBaselineStatus.STOPPED
            FreightInvoiceBaselineStatus.DRAINING -> return next == FreightInvoiceBaselineStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FreightInvoiceBaselineStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedExposure(): Double = round(version.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): FreightInvoiceBaseline {
        return FreightInvoiceBaseline(
            code = code,
            label = label,
            checksum = checksum,
            endsAt = endsAt,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a freight invoice baseline in status idle. */
fun newFreightInvoiceBaseline(code: String, label: String): FreightInvoiceBaseline {
    return FreightInvoiceBaseline(
        code = code,
        label = label,
        checksum = "",
        endsAt = 0,
        version = 0,
        status = FreightInvoiceBaselineStatus.IDLE,
    )
}
