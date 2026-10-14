package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy freight invoice notice is in its life cycle. */
enum class LegacyFreightInvoiceNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy freight invoice notice. Its code is unique within a tenant. */
data class LegacyFreightInvoiceNotice(
    var code: String,
    var title: String,
    var externalRef: String,
    var contactEmail: String,
    var label: String,
    var currency: String,
    var odometer: Int,
    var lane: String,
    var retries: Int,
    var status: LegacyFreightInvoiceNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyFreightInvoiceNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyFreightInvoiceNoticeStatus): Boolean {
        when (status) {
            LegacyFreightInvoiceNoticeStatus.PLANNED -> {
                when (next) {
                    LegacyFreightInvoiceNoticeStatus.SCHEDULED, LegacyFreightInvoiceNoticeStatus.COMPLETED -> return true
                }
                return false
            }
            LegacyFreightInvoiceNoticeStatus.SCHEDULED -> {
                when (next) {
                    LegacyFreightInvoiceNoticeStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LegacyFreightInvoiceNoticeStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyFreightInvoiceNoticeStatus.COMPLETED -> return true
                }
                return false
            }
            LegacyFreightInvoiceNoticeStatus.COMPLETED -> return next == LegacyFreightInvoiceNoticeStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyFreightInvoiceNoticeStatus) {
        if (!canMoveTo(next)) {
            error("legacy freight invoice notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy freight invoice notice"} $code"

    fun effectiveValue(): Double = round((odometer.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): LegacyFreightInvoiceNotice {
        return LegacyFreightInvoiceNotice(
            code = code,
            title = title,
            externalRef = externalRef,
            contactEmail = contactEmail,
            label = label,
            currency = currency,
            odometer = odometer,
            lane = lane,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a legacy freight invoice notice in status planned. */
fun newLegacyFreightInvoiceNotice(code: String, title: String): LegacyFreightInvoiceNotice {
    return LegacyFreightInvoiceNotice(
        code = code,
        title = title,
        externalRef = "",
        contactEmail = "",
        label = "",
        currency = "",
        odometer = 0,
        lane = "",
        retries = 0,
        status = LegacyFreightInvoiceNoticeStatus.PLANNED,
    )
}
