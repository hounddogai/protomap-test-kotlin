package com.acme.transport.lastmile.drop.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop audit is in its life cycle. */
enum class DropAuditStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One drop audit, with what the planners need to act on it. */
data class DropAudit(
    var code: String,
    var name: String,
    var notes: String,
    var ratio: Double,
    var locale: String,
    var quantity: Int,
    var status: DropAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropAuditStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DropAuditStatus): Boolean {
        when (status) {
            DropAuditStatus.PROPOSED -> {
                when (next) {
                    DropAuditStatus.APPROVED, DropAuditStatus.REJECTED -> return true
                }
                return false
            }
            DropAuditStatus.APPROVED -> return next == DropAuditStatus.REJECTED
            DropAuditStatus.REJECTED -> return next == DropAuditStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DropAuditStatus) {
        if (!canMoveTo(next)) {
            error("drop audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedLoad(): Double = round((quantity.toDouble() - ratio) * 100) / 100

    fun snapshot(): DropAudit {
        return DropAudit(
            code = code,
            name = name,
            notes = notes,
            ratio = ratio,
            locale = locale,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a drop audit in status proposed. */
fun newDropAudit(code: String, name: String): DropAudit {
    return DropAudit(
        code = code,
        name = name,
        notes = "",
        ratio = 0.0,
        locale = "",
        quantity = 0,
        status = DropAuditStatus.PROPOSED,
    )
}
