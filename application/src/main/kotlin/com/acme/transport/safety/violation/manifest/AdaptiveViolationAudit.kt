package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive violation audit is in its life cycle. */
enum class AdaptiveViolationAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A adaptive violation audit. Its code is unique within a tenant. */
data class AdaptiveViolationAudit(
    var code: String,
    var name: String,
    var attempts: Int,
    var label: String,
    var limit: Int,
    var createdAt: Int,
    var status: AdaptiveViolationAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveViolationAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveViolationAuditStatus): Boolean {
        when (status) {
            AdaptiveViolationAuditStatus.OPEN -> {
                when (next) {
                    AdaptiveViolationAuditStatus.IN_REVIEW, AdaptiveViolationAuditStatus.RESOLVED -> return true
                }
                return false
            }
            AdaptiveViolationAuditStatus.IN_REVIEW -> return next == AdaptiveViolationAuditStatus.RESOLVED
            AdaptiveViolationAuditStatus.RESOLVED -> {
                when (next) {
                    AdaptiveViolationAuditStatus.REOPENED -> return true
                }
                return false
            }
            AdaptiveViolationAuditStatus.REOPENED -> {
                when (next) {
                    AdaptiveViolationAuditStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveViolationAuditStatus) {
        if (!canMoveTo(next)) {
            error("adaptive violation audit $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun effectiveLoad(): Double = round((createdAt.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveViolationAudit {
        return AdaptiveViolationAudit(
            code = code,
            name = name,
            attempts = attempts,
            label = label,
            limit = limit,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive violation audit in status open. */
fun newAdaptiveViolationAudit(code: String, name: String): AdaptiveViolationAudit {
    return AdaptiveViolationAudit(
        code = code,
        name = name,
        attempts = 0,
        label = "",
        limit = 0,
        createdAt = 0,
        status = AdaptiveViolationAuditStatus.OPEN,
    )
}
