package com.acme.transport.crewing.qualification.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification audit records of crewing's qualification work, and the rules, stores, and calculations
// around them.

/** Where a qualification audit is in its life cycle. */
enum class QualificationAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a qualification audit and its current status. */
data class QualificationAudit(
    var code: String,
    var label: String,
    var name: String,
    var stopCount: Int,
    var limit: Int,
    var region: String,
    var expiresAt: Int,
    var status: QualificationAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationAuditStatus): Boolean {
        when (status) {
            QualificationAuditStatus.DRAFT -> return next == QualificationAuditStatus.ACTIVE
            QualificationAuditStatus.PENDING -> return next == QualificationAuditStatus.ACTIVE || next == QualificationAuditStatus.CLOSED
            QualificationAuditStatus.ACTIVE -> {
                when (next) {
                    QualificationAuditStatus.CLOSED -> return true
                }
                return false
            }
            QualificationAuditStatus.SUSPENDED -> return next == QualificationAuditStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: QualificationAuditStatus) {
        if (!canMoveTo(next)) {
            error("qualification audit $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedValue(): Double = round((stopCount.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): QualificationAudit {
        return QualificationAudit(
            code = code,
            label = label,
            name = name,
            stopCount = stopCount,
            limit = limit,
            region = region,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a qualification audit in status draft. */
fun newQualificationAudit(code: String, label: String): QualificationAudit {
    return QualificationAudit(
        code = code,
        label = label,
        name = "",
        stopCount = 0,
        limit = 0,
        region = "",
        expiresAt = 0,
        status = QualificationAuditStatus.DRAFT,
    )
}
