package com.acme.transport.freightpay.audit.template

import com.acme.transport.kit.text.normalize

/** Where a audit slot is in its life cycle. */
enum class AuditSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A audit slot as the audit team keeps it. */
data class AuditSlot(
    var code: String,
    var label: String,
    var ownerId: String,
    var timezone: String,
    var title: String,
    var quantity: Int,
    var status: AuditSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditSlotStatus): Boolean {
        when (status) {
            AuditSlotStatus.OPEN -> {
                when (next) {
                    AuditSlotStatus.REOPENED, AuditSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            AuditSlotStatus.IN_REVIEW -> return next == AuditSlotStatus.RESOLVED || next == AuditSlotStatus.ARCHIVED
            AuditSlotStatus.RESOLVED -> return next == AuditSlotStatus.REOPENED || next == AuditSlotStatus.ARCHIVED
            AuditSlotStatus.REOPENED -> {
                when (next) {
                    AuditSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditSlotStatus) {
        if (!canMoveTo(next)) {
            error("audit slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): AuditSlot {
        return AuditSlot(
            code = code,
            label = label,
            ownerId = ownerId,
            timezone = timezone,
            title = title,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a audit slot in status open. */
fun newAuditSlot(code: String, label: String): AuditSlot {
    return AuditSlot(
        code = code,
        label = label,
        ownerId = "",
        timezone = "",
        title = "",
        quantity = 0,
        status = AuditSlotStatus.OPEN,
    )
}
