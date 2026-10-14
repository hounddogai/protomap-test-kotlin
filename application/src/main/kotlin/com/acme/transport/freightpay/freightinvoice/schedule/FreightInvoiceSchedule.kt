package com.acme.transport.freightpay.freightinvoice.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The freight invoice schedule records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a freight invoice schedule is in its life cycle. */
enum class FreightInvoiceScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One freight invoice schedule, with what the planners need to act on it. */
data class FreightInvoiceSchedule(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var shiftCode: String,
    var locale: String,
    var expiresAt: Int,
    var segment: String,
    var status: FreightInvoiceScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceScheduleStatus): Boolean {
        when (status) {
            FreightInvoiceScheduleStatus.OPEN -> {
                when (next) {
                    FreightInvoiceScheduleStatus.IN_REVIEW, FreightInvoiceScheduleStatus.RESOLVED -> return true
                }
                return false
            }
            FreightInvoiceScheduleStatus.IN_REVIEW -> {
                when (next) {
                    FreightInvoiceScheduleStatus.RESOLVED, FreightInvoiceScheduleStatus.REOPENED -> return true
                }
                return false
            }
            FreightInvoiceScheduleStatus.RESOLVED -> return next == FreightInvoiceScheduleStatus.REOPENED
            FreightInvoiceScheduleStatus.REOPENED -> {
                when (next) {
                    FreightInvoiceScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceScheduleStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice schedule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netLoad(): Double = round(durationMinutes.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): FreightInvoiceSchedule {
        return FreightInvoiceSchedule(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            shiftCode = shiftCode,
            locale = locale,
            expiresAt = expiresAt,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a freight invoice schedule in status open. */
fun newFreightInvoiceSchedule(code: String, title: String): FreightInvoiceSchedule {
    return FreightInvoiceSchedule(
        code = code,
        title = title,
        durationMinutes = 0,
        shiftCode = "",
        locale = "",
        expiresAt = 0,
        segment = "",
        status = FreightInvoiceScheduleStatus.OPEN,
    )
}
