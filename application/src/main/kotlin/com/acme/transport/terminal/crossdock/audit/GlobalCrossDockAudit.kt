package com.acme.transport.terminal.crossdock.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global cross dock audit records of terminal's cross dock work, and the rules, stores, and calculations
// around them.

/** Where a global cross dock audit is in its life cycle. */
enum class GlobalCrossDockAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One global cross dock audit, with what the planners need to act on it. */
data class GlobalCrossDockAudit(
    var code: String,
    var label: String,
    var maximum: Double,
    var contactEmail: String,
    var stopCount: Int,
    var name: String,
    var expiresAt: Int,
    var category: String,
    var margin: Double,
    var active: Boolean,
    var windowDays: Int,
    var status: GlobalCrossDockAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCrossDockAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCrossDockAuditStatus): Boolean {
        when (status) {
            GlobalCrossDockAuditStatus.PLANNED -> return next == GlobalCrossDockAuditStatus.CANCELLED
            GlobalCrossDockAuditStatus.SCHEDULED -> {
                when (next) {
                    GlobalCrossDockAuditStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalCrossDockAuditStatus.IN_PROGRESS -> {
                when (next) {
                    GlobalCrossDockAuditStatus.COMPLETED -> return true
                }
                return false
            }
            GlobalCrossDockAuditStatus.COMPLETED -> {
                when (next) {
                    GlobalCrossDockAuditStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCrossDockAuditStatus) {
        if (!canMoveTo(next)) {
            error("global cross dock audit $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round(expiresAt.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): GlobalCrossDockAudit {
        return GlobalCrossDockAudit(
            code = code,
            label = label,
            maximum = maximum,
            contactEmail = contactEmail,
            stopCount = stopCount,
            name = name,
            expiresAt = expiresAt,
            category = category,
            margin = margin,
            active = active,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a global cross dock audit in status planned. */
fun newGlobalCrossDockAudit(code: String, label: String): GlobalCrossDockAudit {
    return GlobalCrossDockAudit(
        code = code,
        label = label,
        maximum = 0.0,
        contactEmail = "",
        stopCount = 0,
        name = "",
        expiresAt = 0,
        category = "",
        margin = 0.0,
        active = false,
        windowDays = 0,
        status = GlobalCrossDockAuditStatus.PLANNED,
    )
}
