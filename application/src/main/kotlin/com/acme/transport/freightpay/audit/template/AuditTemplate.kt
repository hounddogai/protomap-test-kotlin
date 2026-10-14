package com.acme.transport.freightpay.audit.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The audit template records of freightpay's audit work, and the rules, stores, and calculations around them.

/** Where a audit template is in its life cycle. */
enum class AuditTemplateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A audit template. Its code is unique within a tenant. */
data class AuditTemplate(
    var code: String,
    var label: String,
    var revision: Int,
    var priority: Int,
    var shiftCode: String,
    var status: AuditTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditTemplateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditTemplateStatus): Boolean {
        when (status) {
            AuditTemplateStatus.PROPOSED -> return next == AuditTemplateStatus.REJECTED
            AuditTemplateStatus.APPROVED -> return next == AuditTemplateStatus.REJECTED
            AuditTemplateStatus.REJECTED -> return next == AuditTemplateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AuditTemplateStatus) {
        if (!canMoveTo(next)) {
            error("audit template $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedExposure(): Double = round((revision.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): AuditTemplate {
        return AuditTemplate(
            code = code,
            label = label,
            revision = revision,
            priority = priority,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a audit template in status proposed. */
fun newAuditTemplate(code: String, label: String): AuditTemplate {
    return AuditTemplate(
        code = code,
        label = label,
        revision = 0,
        priority = 0,
        shiftCode = "",
        status = AuditTemplateStatus.PROPOSED,
    )
}
