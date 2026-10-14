package com.acme.transport.freightpay.freightinvoice.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice notice is in its life cycle. */
enum class FreightInvoiceNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One freight invoice notice, with what the planners need to act on it. */
data class FreightInvoiceNotice(
    var code: String,
    var name: String,
    var visible: Boolean,
    var amount: Double,
    var version: Int,
    var depot: String,
    var segment: String,
    var status: FreightInvoiceNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceNoticeStatus): Boolean {
        when (status) {
            FreightInvoiceNoticeStatus.IDLE -> {
                when (next) {
                    FreightInvoiceNoticeStatus.WARMING, FreightInvoiceNoticeStatus.STOPPED -> return true
                }
                return false
            }
            FreightInvoiceNoticeStatus.WARMING -> {
                when (next) {
                    FreightInvoiceNoticeStatus.READY, FreightInvoiceNoticeStatus.DRAINING -> return true
                }
                return false
            }
            FreightInvoiceNoticeStatus.READY -> return next == FreightInvoiceNoticeStatus.DRAINING || next == FreightInvoiceNoticeStatus.STOPPED
            FreightInvoiceNoticeStatus.DRAINING -> {
                when (next) {
                    FreightInvoiceNoticeStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceNoticeStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice notice $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun adjustedLoad(): Double = round((amount + version.toDouble()) * 100) / 100

    fun snapshot(): FreightInvoiceNotice {
        return FreightInvoiceNotice(
            code = code,
            name = name,
            visible = visible,
            amount = amount,
            version = version,
            depot = depot,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a freight invoice notice in status idle. */
fun newFreightInvoiceNotice(code: String, name: String): FreightInvoiceNotice {
    return FreightInvoiceNotice(
        code = code,
        name = name,
        visible = false,
        amount = 0.0,
        version = 0,
        depot = "",
        segment = "",
        status = FreightInvoiceNoticeStatus.IDLE,
    )
}
