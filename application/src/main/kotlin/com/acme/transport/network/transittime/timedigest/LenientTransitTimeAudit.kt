package com.acme.transport.network.transittime.timedigest

import com.acme.transport.kit.text.normalize

/** Where a lenient transit time audit is in its life cycle. */
enum class LenientTransitTimeAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lenient transit time audit. Its code is unique within a tenant. */
data class LenientTransitTimeAudit(
    var code: String,
    var title: String,
    var active: Boolean,
    var driverId: String,
    var category: String,
    var quantity: Int,
    var status: LenientTransitTimeAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientTransitTimeAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientTransitTimeAuditStatus): Boolean {
        when (status) {
            LenientTransitTimeAuditStatus.OPEN -> return next == LenientTransitTimeAuditStatus.IN_REVIEW || next == LenientTransitTimeAuditStatus.RESOLVED
            LenientTransitTimeAuditStatus.IN_REVIEW -> {
                when (next) {
                    LenientTransitTimeAuditStatus.REOPENED, LenientTransitTimeAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientTransitTimeAuditStatus.RESOLVED -> {
                when (next) {
                    LenientTransitTimeAuditStatus.REOPENED, LenientTransitTimeAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientTransitTimeAuditStatus.REOPENED -> {
                when (next) {
                    LenientTransitTimeAuditStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientTransitTimeAuditStatus) {
        if (!canMoveTo(next)) {
            error("lenient transit time audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): LenientTransitTimeAudit {
        return LenientTransitTimeAudit(
            code = code,
            title = title,
            active = active,
            driverId = driverId,
            category = category,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lenient transit time audit in status open. */
fun newLenientTransitTimeAudit(code: String, title: String): LenientTransitTimeAudit {
    return LenientTransitTimeAudit(
        code = code,
        title = title,
        active = false,
        driverId = "",
        category = "",
        quantity = 0,
        status = LenientTransitTimeAuditStatus.OPEN,
    )
}
