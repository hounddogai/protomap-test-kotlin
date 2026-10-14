package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation audit is in its life cycle. */
enum class ViolationAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One violation audit, with what the planners need to act on it. */
data class ViolationAudit(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var currency: String,
    var stopCount: Int,
    var floorValue: Double,
    var status: ViolationAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationAuditStatus): Boolean {
        when (status) {
            ViolationAuditStatus.OPEN -> return next == ViolationAuditStatus.IN_REVIEW || next == ViolationAuditStatus.REOPENED
            ViolationAuditStatus.IN_REVIEW -> return next == ViolationAuditStatus.RESOLVED || next == ViolationAuditStatus.REOPENED
            ViolationAuditStatus.RESOLVED -> return next == ViolationAuditStatus.ARCHIVED
            ViolationAuditStatus.REOPENED -> {
                when (next) {
                    ViolationAuditStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationAuditStatus) {
        if (!canMoveTo(next)) {
            error("violation audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round(stopCount.toDouble() * floorValue * 100) / 100

    fun snapshot(): ViolationAudit {
        return ViolationAudit(
            code = code,
            title = title,
            attributes = attributes,
            currency = currency,
            stopCount = stopCount,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a violation audit in status open. */
fun newViolationAudit(code: String, title: String): ViolationAudit {
    return ViolationAudit(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        currency = "",
        stopCount = 0,
        floorValue = 0.0,
        status = ViolationAuditStatus.OPEN,
    )
}
