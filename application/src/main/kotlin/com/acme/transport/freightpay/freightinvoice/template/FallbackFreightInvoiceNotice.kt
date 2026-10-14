package com.acme.transport.freightpay.freightinvoice.template

import com.acme.transport.kit.text.normalize

/** Where a fallback freight invoice notice is in its life cycle. */
enum class FallbackFreightInvoiceNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback freight invoice notice. Its code is unique within a tenant. */
data class FallbackFreightInvoiceNotice(
    var code: String,
    var name: String,
    var region: String,
    var vehicleId: String,
    var windowDays: Int,
    var status: FallbackFreightInvoiceNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackFreightInvoiceNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackFreightInvoiceNoticeStatus): Boolean {
        when (status) {
            FallbackFreightInvoiceNoticeStatus.CREATED -> return next == FallbackFreightInvoiceNoticeStatus.PUBLISHED || next == FallbackFreightInvoiceNoticeStatus.RETIRED
            FallbackFreightInvoiceNoticeStatus.VALIDATED -> {
                when (next) {
                    FallbackFreightInvoiceNoticeStatus.PUBLISHED -> return true
                }
                return false
            }
            FallbackFreightInvoiceNoticeStatus.PUBLISHED -> {
                when (next) {
                    FallbackFreightInvoiceNoticeStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackFreightInvoiceNoticeStatus) {
        if (!canMoveTo(next)) {
            error("fallback freight invoice notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): FallbackFreightInvoiceNotice {
        return FallbackFreightInvoiceNotice(
            code = code,
            name = name,
            region = region,
            vehicleId = vehicleId,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback freight invoice notice in status created. */
fun newFallbackFreightInvoiceNotice(code: String, name: String): FallbackFreightInvoiceNotice {
    return FallbackFreightInvoiceNotice(
        code = code,
        name = name,
        region = "",
        vehicleId = "",
        windowDays = 0,
        status = FallbackFreightInvoiceNoticeStatus.CREATED,
    )
}
