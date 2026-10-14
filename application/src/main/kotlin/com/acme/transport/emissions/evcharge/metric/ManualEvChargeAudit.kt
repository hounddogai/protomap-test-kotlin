package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual ev charge audit is in its life cycle. */
enum class ManualEvChargeAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a manual ev charge audit and its current status. */
data class ManualEvChargeAudit(
    var code: String,
    var title: String,
    var shiftCode: String,
    var approved: Boolean,
    var threshold: Double,
    var capacity: Int,
    var locale: String,
    var fuelLiters: Double,
    var pallets: Int,
    var status: ManualEvChargeAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualEvChargeAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualEvChargeAuditStatus): Boolean {
        when (status) {
            ManualEvChargeAuditStatus.OPEN -> return next == ManualEvChargeAuditStatus.IN_REVIEW || next == ManualEvChargeAuditStatus.ARCHIVED
            ManualEvChargeAuditStatus.IN_REVIEW -> return next == ManualEvChargeAuditStatus.REOPENED || next == ManualEvChargeAuditStatus.ARCHIVED
            ManualEvChargeAuditStatus.RESOLVED -> {
                when (next) {
                    ManualEvChargeAuditStatus.REOPENED -> return true
                }
                return false
            }
            ManualEvChargeAuditStatus.REOPENED -> {
                when (next) {
                    ManualEvChargeAuditStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualEvChargeAuditStatus) {
        if (!canMoveTo(next)) {
            error("manual ev charge audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedExposure(): Double = round((pallets.toDouble() + threshold) * 100) / 100

    fun snapshot(): ManualEvChargeAudit {
        return ManualEvChargeAudit(
            code = code,
            title = title,
            shiftCode = shiftCode,
            approved = approved,
            threshold = threshold,
            capacity = capacity,
            locale = locale,
            fuelLiters = fuelLiters,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual ev charge audit in status open. */
fun newManualEvChargeAudit(code: String, title: String): ManualEvChargeAudit {
    return ManualEvChargeAudit(
        code = code,
        title = title,
        shiftCode = "",
        approved = false,
        threshold = 0.0,
        capacity = 0,
        locale = "",
        fuelLiters = 0.0,
        pallets = 0,
        status = ManualEvChargeAuditStatus.OPEN,
    )
}
