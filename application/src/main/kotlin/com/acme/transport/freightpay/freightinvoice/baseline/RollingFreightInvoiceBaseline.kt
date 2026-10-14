package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling freight invoice baseline records of freightpay's freight invoice work, and the rules, stores,
// and calculations around them.

/** Where a rolling freight invoice baseline is in its life cycle. */
enum class RollingFreightInvoiceBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One rolling freight invoice baseline, with what the planners need to act on it. */
data class RollingFreightInvoiceBaseline(
    var code: String,
    var title: String,
    var quantity: Int,
    var threshold: Double,
    var timezone: String,
    var shiftCode: String,
    var tags: MutableList<String>,
    var pinned: Boolean,
    var source: String,
    var approved: Boolean,
    var status: RollingFreightInvoiceBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFreightInvoiceBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFreightInvoiceBaselineStatus): Boolean {
        when (status) {
            RollingFreightInvoiceBaselineStatus.DRAFT -> return next == RollingFreightInvoiceBaselineStatus.ACTIVE
            RollingFreightInvoiceBaselineStatus.PENDING -> {
                when (next) {
                    RollingFreightInvoiceBaselineStatus.CLOSED -> return true
                }
                return false
            }
            RollingFreightInvoiceBaselineStatus.ACTIVE -> {
                when (next) {
                    RollingFreightInvoiceBaselineStatus.SUSPENDED, RollingFreightInvoiceBaselineStatus.CLOSED -> return true
                }
                return false
            }
            RollingFreightInvoiceBaselineStatus.SUSPENDED -> {
                when (next) {
                    RollingFreightInvoiceBaselineStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingFreightInvoiceBaselineStatus) {
        if (!canMoveTo(next)) {
            error("rolling freight invoice baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveExposure(): Double = round((quantity.toDouble() - threshold) * 100) / 100

    fun snapshot(): RollingFreightInvoiceBaseline {
        return RollingFreightInvoiceBaseline(
            code = code,
            title = title,
            quantity = quantity,
            threshold = threshold,
            timezone = timezone,
            shiftCode = shiftCode,
            tags = tags,
            pinned = pinned,
            source = source,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rolling freight invoice baseline in status draft. */
fun newRollingFreightInvoiceBaseline(code: String, title: String): RollingFreightInvoiceBaseline {
    return RollingFreightInvoiceBaseline(
        code = code,
        title = title,
        quantity = 0,
        threshold = 0.0,
        timezone = "",
        shiftCode = "",
        tags = mutableListOf<String>(),
        pinned = false,
        source = "",
        approved = false,
        status = RollingFreightInvoiceBaselineStatus.DRAFT,
    )
}
