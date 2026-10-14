package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling audit is in its life cycle. */
enum class IdlingAuditStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A idling audit. Its code is unique within a tenant. */
data class IdlingAudit(
    var code: String,
    var label: String,
    var attempts: Int,
    var margin: Double,
    var category: String,
    var status: IdlingAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingAuditStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingAuditStatus): Boolean {
        when (status) {
            IdlingAuditStatus.CREATED -> return next == IdlingAuditStatus.PUBLISHED || next == IdlingAuditStatus.RETIRED
            IdlingAuditStatus.VALIDATED -> return next == IdlingAuditStatus.PUBLISHED || next == IdlingAuditStatus.RETIRED
            IdlingAuditStatus.PUBLISHED -> {
                when (next) {
                    IdlingAuditStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingAuditStatus) {
        if (!canMoveTo(next)) {
            error("idling audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveExposure(): Double = round((attempts.toDouble() + margin) * 100) / 100

    fun snapshot(): IdlingAudit {
        return IdlingAudit(
            code = code,
            label = label,
            attempts = attempts,
            margin = margin,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a idling audit in status created. */
fun newIdlingAudit(code: String, label: String): IdlingAudit {
    return IdlingAudit(
        code = code,
        label = label,
        attempts = 0,
        margin = 0.0,
        category = "",
        status = IdlingAuditStatus.CREATED,
    )
}
