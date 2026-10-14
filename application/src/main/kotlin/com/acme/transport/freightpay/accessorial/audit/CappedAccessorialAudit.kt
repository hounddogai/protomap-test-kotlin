package com.acme.transport.freightpay.accessorial.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped accessorial audit records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a capped accessorial audit is in its life cycle. */
enum class CappedAccessorialAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped accessorial audit as the accessorial team keeps it. */
data class CappedAccessorialAudit(
    var code: String,
    var label: String,
    var phone: String,
    var driverId: String,
    var budget: Double,
    var attributes: MutableMap<String, String>,
    var amount: Double,
    var dock: String,
    var status: CappedAccessorialAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAccessorialAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAccessorialAuditStatus): Boolean {
        when (status) {
            CappedAccessorialAuditStatus.DRAFT -> return next == CappedAccessorialAuditStatus.SUSPENDED
            CappedAccessorialAuditStatus.PENDING -> {
                when (next) {
                    CappedAccessorialAuditStatus.ACTIVE, CappedAccessorialAuditStatus.CLOSED -> return true
                }
                return false
            }
            CappedAccessorialAuditStatus.ACTIVE -> return next == CappedAccessorialAuditStatus.CLOSED
            CappedAccessorialAuditStatus.SUSPENDED -> return next == CappedAccessorialAuditStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedAccessorialAuditStatus) {
        if (!canMoveTo(next)) {
            error("capped accessorial audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"capped accessorial audit"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round(amount * budget * 100) / 100

    fun snapshot(): CappedAccessorialAudit {
        return CappedAccessorialAudit(
            code = code,
            label = label,
            phone = phone,
            driverId = driverId,
            budget = budget,
            attributes = attributes,
            amount = amount,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped accessorial audit in status draft. */
fun newCappedAccessorialAudit(code: String, label: String): CappedAccessorialAudit {
    return CappedAccessorialAudit(
        code = code,
        label = label,
        phone = "",
        driverId = "",
        budget = 0.0,
        attributes = mutableMapOf<String, String>(),
        amount = 0.0,
        dock = "",
        status = CappedAccessorialAuditStatus.DRAFT,
    )
}
