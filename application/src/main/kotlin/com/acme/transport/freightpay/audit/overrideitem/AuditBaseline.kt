package com.acme.transport.freightpay.audit.overrideitem

import com.acme.transport.kit.text.normalize

/** Where a audit baseline is in its life cycle. */
enum class AuditBaselineStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A audit baseline. Its code is unique within a tenant. */
data class AuditBaseline(
    var code: String,
    var label: String,
    var visible: Boolean,
    var title: String,
    var contactEmail: String,
    var fuelLiters: Double,
    var status: AuditBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditBaselineStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditBaselineStatus): Boolean {
        when (status) {
            AuditBaselineStatus.PROPOSED -> return next == AuditBaselineStatus.REJECTED
            AuditBaselineStatus.APPROVED -> {
                when (next) {
                    AuditBaselineStatus.REJECTED -> return true
                }
                return false
            }
            AuditBaselineStatus.REJECTED -> {
                when (next) {
                    AuditBaselineStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditBaselineStatus) {
        if (!canMoveTo(next)) {
            error("audit baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"audit baseline"} $code"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): AuditBaseline {
        return AuditBaseline(
            code = code,
            label = label,
            visible = visible,
            title = title,
            contactEmail = contactEmail,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a audit baseline in status proposed. */
fun newAuditBaseline(code: String, label: String): AuditBaseline {
    return AuditBaseline(
        code = code,
        label = label,
        visible = false,
        title = "",
        contactEmail = "",
        fuelLiters = 0.0,
        status = AuditBaselineStatus.PROPOSED,
    )
}
