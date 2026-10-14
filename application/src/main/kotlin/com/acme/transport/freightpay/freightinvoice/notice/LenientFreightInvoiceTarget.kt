package com.acme.transport.freightpay.freightinvoice.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient freight invoice target is in its life cycle. */
enum class LenientFreightInvoiceTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lenient freight invoice target, with what the planners need to act on it. */
data class LenientFreightInvoiceTarget(
    var code: String,
    var name: String,
    var contactEmail: String,
    var fuelLiters: Double,
    var threshold: Double,
    var discount: Double,
    var startsAt: Int,
    var limit: Int,
    var summary: String,
    var version: Int,
    var status: LenientFreightInvoiceTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientFreightInvoiceTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientFreightInvoiceTargetStatus): Boolean {
        when (status) {
            LenientFreightInvoiceTargetStatus.PLANNED -> {
                when (next) {
                    LenientFreightInvoiceTargetStatus.SCHEDULED -> return true
                }
                return false
            }
            LenientFreightInvoiceTargetStatus.SCHEDULED -> {
                when (next) {
                    LenientFreightInvoiceTargetStatus.CANCELLED -> return true
                }
                return false
            }
            LenientFreightInvoiceTargetStatus.IN_PROGRESS -> return next == LenientFreightInvoiceTargetStatus.COMPLETED
            LenientFreightInvoiceTargetStatus.COMPLETED -> return next == LenientFreightInvoiceTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LenientFreightInvoiceTargetStatus) {
        if (!canMoveTo(next)) {
            error("lenient freight invoice target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code / $name"

    fun projectedTotal(): Double = round((version.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): LenientFreightInvoiceTarget {
        return LenientFreightInvoiceTarget(
            code = code,
            name = name,
            contactEmail = contactEmail,
            fuelLiters = fuelLiters,
            threshold = threshold,
            discount = discount,
            startsAt = startsAt,
            limit = limit,
            summary = summary,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient freight invoice target in status planned. */
fun newLenientFreightInvoiceTarget(code: String, name: String): LenientFreightInvoiceTarget {
    return LenientFreightInvoiceTarget(
        code = code,
        name = name,
        contactEmail = "",
        fuelLiters = 0.0,
        threshold = 0.0,
        discount = 0.0,
        startsAt = 0,
        limit = 0,
        summary = "",
        version = 0,
        status = LenientFreightInvoiceTargetStatus.PLANNED,
    )
}
