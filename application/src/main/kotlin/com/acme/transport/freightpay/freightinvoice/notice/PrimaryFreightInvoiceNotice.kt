package com.acme.transport.freightpay.freightinvoice.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary freight invoice notice records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a primary freight invoice notice is in its life cycle. */
enum class PrimaryFreightInvoiceNoticeStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a primary freight invoice notice and its current status. */
data class PrimaryFreightInvoiceNotice(
    var code: String,
    var label: String,
    var description: String,
    var title: String,
    var carrier: String,
    var startsAt: Int,
    var windowDays: Int,
    var status: PrimaryFreightInvoiceNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryFreightInvoiceNoticeStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryFreightInvoiceNoticeStatus): Boolean {
        when (status) {
            PrimaryFreightInvoiceNoticeStatus.OPEN -> {
                when (next) {
                    PrimaryFreightInvoiceNoticeStatus.RESOLVED, PrimaryFreightInvoiceNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryFreightInvoiceNoticeStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryFreightInvoiceNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryFreightInvoiceNoticeStatus.RESOLVED -> return next == PrimaryFreightInvoiceNoticeStatus.REOPENED || next == PrimaryFreightInvoiceNoticeStatus.ARCHIVED
            PrimaryFreightInvoiceNoticeStatus.REOPENED -> {
                when (next) {
                    PrimaryFreightInvoiceNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryFreightInvoiceNoticeStatus) {
        if (!canMoveTo(next)) {
            error("primary freight invoice notice $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveExposure(): Double = round((startsAt.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): PrimaryFreightInvoiceNotice {
        return PrimaryFreightInvoiceNotice(
            code = code,
            label = label,
            description = description,
            title = title,
            carrier = carrier,
            startsAt = startsAt,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary freight invoice notice in status open. */
fun newPrimaryFreightInvoiceNotice(code: String, label: String): PrimaryFreightInvoiceNotice {
    return PrimaryFreightInvoiceNotice(
        code = code,
        label = label,
        description = "",
        title = "",
        carrier = "",
        startsAt = 0,
        windowDays = 0,
        status = PrimaryFreightInvoiceNoticeStatus.OPEN,
    )
}
