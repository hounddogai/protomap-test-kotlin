package com.acme.transport.customs.inspection.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional inspection audit records of customs's inspection work, and the rules, stores, and calculations
// around them.

/** Where a regional inspection audit is in its life cycle. */
enum class RegionalInspectionAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A regional inspection audit. Its code is unique within a tenant. */
data class RegionalInspectionAudit(
    var code: String,
    var name: String,
    var channel: String,
    var country: String,
    var visible: Boolean,
    var source: String,
    var margin: Double,
    var createdAt: Int,
    var sequence: Int,
    var updatedAt: Int,
    var status: RegionalInspectionAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalInspectionAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalInspectionAuditStatus): Boolean {
        when (status) {
            RegionalInspectionAuditStatus.OPEN -> {
                when (next) {
                    RegionalInspectionAuditStatus.RESOLVED -> return true
                }
                return false
            }
            RegionalInspectionAuditStatus.IN_REVIEW -> {
                when (next) {
                    RegionalInspectionAuditStatus.RESOLVED, RegionalInspectionAuditStatus.REOPENED -> return true
                }
                return false
            }
            RegionalInspectionAuditStatus.RESOLVED -> {
                when (next) {
                    RegionalInspectionAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalInspectionAuditStatus.REOPENED -> {
                when (next) {
                    RegionalInspectionAuditStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalInspectionAuditStatus) {
        if (!canMoveTo(next)) {
            error("regional inspection audit $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional inspection audit"} $code"

    fun projectedExposure(): Double = round((createdAt.toDouble() - margin) * 100) / 100

    fun snapshot(): RegionalInspectionAudit {
        return RegionalInspectionAudit(
            code = code,
            name = name,
            channel = channel,
            country = country,
            visible = visible,
            source = source,
            margin = margin,
            createdAt = createdAt,
            sequence = sequence,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional inspection audit in status open. */
fun newRegionalInspectionAudit(code: String, name: String): RegionalInspectionAudit {
    return RegionalInspectionAudit(
        code = code,
        name = name,
        channel = "",
        country = "",
        visible = false,
        source = "",
        margin = 0.0,
        createdAt = 0,
        sequence = 0,
        updatedAt = 0,
        status = RegionalInspectionAuditStatus.OPEN,
    )
}
