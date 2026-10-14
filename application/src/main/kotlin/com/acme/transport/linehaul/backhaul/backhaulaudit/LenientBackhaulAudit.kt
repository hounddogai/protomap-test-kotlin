package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.text.normalize

// The lenient backhaul audit records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a lenient backhaul audit is in its life cycle. */
enum class LenientBackhaulAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One lenient backhaul audit, with what the planners need to act on it. */
data class LenientBackhaulAudit(
    var code: String,
    var name: String,
    var visible: Boolean,
    var shiftCode: String,
    var driverId: String,
    var amount: Double,
    var status: LenientBackhaulAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientBackhaulAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientBackhaulAuditStatus): Boolean {
        when (status) {
            LenientBackhaulAuditStatus.DRAFT -> return next == LenientBackhaulAuditStatus.PENDING
            LenientBackhaulAuditStatus.PENDING -> return next == LenientBackhaulAuditStatus.SUSPENDED || next == LenientBackhaulAuditStatus.CLOSED
            LenientBackhaulAuditStatus.ACTIVE -> return next == LenientBackhaulAuditStatus.SUSPENDED || next == LenientBackhaulAuditStatus.CLOSED
            LenientBackhaulAuditStatus.SUSPENDED -> {
                when (next) {
                    LenientBackhaulAuditStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientBackhaulAuditStatus) {
        if (!canMoveTo(next)) {
            error("lenient backhaul audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient backhaul audit"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): LenientBackhaulAudit {
        return LenientBackhaulAudit(
            code = code,
            name = name,
            visible = visible,
            shiftCode = shiftCode,
            driverId = driverId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient backhaul audit in status draft. */
fun newLenientBackhaulAudit(code: String, name: String): LenientBackhaulAudit {
    return LenientBackhaulAudit(
        code = code,
        name = name,
        visible = false,
        shiftCode = "",
        driverId = "",
        amount = 0.0,
        status = LenientBackhaulAuditStatus.DRAFT,
    )
}
