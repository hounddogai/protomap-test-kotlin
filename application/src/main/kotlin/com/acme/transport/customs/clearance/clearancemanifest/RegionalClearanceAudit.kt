package com.acme.transport.customs.clearance.clearancemanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional clearance audit is in its life cycle. */
enum class RegionalClearanceAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One regional clearance audit, with what the planners need to act on it. */
data class RegionalClearanceAudit(
    var code: String,
    var name: String,
    var threshold: Double,
    var archived: Boolean,
    var source: String,
    var startsAt: Int,
    var score: Double,
    var label: String,
    var expiresAt: Int,
    var status: RegionalClearanceAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalClearanceAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalClearanceAuditStatus): Boolean {
        when (status) {
            RegionalClearanceAuditStatus.PLANNED -> return next == RegionalClearanceAuditStatus.SCHEDULED || next == RegionalClearanceAuditStatus.CANCELLED
            RegionalClearanceAuditStatus.SCHEDULED -> {
                when (next) {
                    RegionalClearanceAuditStatus.IN_PROGRESS, RegionalClearanceAuditStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalClearanceAuditStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalClearanceAuditStatus.COMPLETED, RegionalClearanceAuditStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalClearanceAuditStatus.COMPLETED -> {
                when (next) {
                    RegionalClearanceAuditStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalClearanceAuditStatus) {
        if (!canMoveTo(next)) {
            error("regional clearance audit $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun effectiveTotal(): Double = round(expiresAt.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): RegionalClearanceAudit {
        return RegionalClearanceAudit(
            code = code,
            name = name,
            threshold = threshold,
            archived = archived,
            source = source,
            startsAt = startsAt,
            score = score,
            label = label,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional clearance audit in status planned. */
fun newRegionalClearanceAudit(code: String, name: String): RegionalClearanceAudit {
    return RegionalClearanceAudit(
        code = code,
        name = name,
        threshold = 0.0,
        archived = false,
        source = "",
        startsAt = 0,
        score = 0.0,
        label = "",
        expiresAt = 0,
        status = RegionalClearanceAuditStatus.PLANNED,
    )
}
