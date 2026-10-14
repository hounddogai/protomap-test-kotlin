package com.acme.transport.customs.clearance.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance audit is in its life cycle. */
enum class ClearanceAuditStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One clearance audit, with what the planners need to act on it. */
data class ClearanceAudit(
    var code: String,
    var title: String,
    var description: String,
    var score: Double,
    var attributes: MutableMap<String, String>,
    var reviewer: String,
    var name: String,
    var updatedAt: Int,
    var cost: Double,
    var status: ClearanceAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceAuditStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceAuditStatus): Boolean {
        when (status) {
            ClearanceAuditStatus.PROPOSED -> {
                when (next) {
                    ClearanceAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
            ClearanceAuditStatus.APPROVED -> {
                when (next) {
                    ClearanceAuditStatus.REJECTED, ClearanceAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
            ClearanceAuditStatus.REJECTED -> return next == ClearanceAuditStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ClearanceAuditStatus) {
        if (!canMoveTo(next)) {
            error("clearance audit $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedValue(): Double = round((score + cost) * 100) / 100

    fun snapshot(): ClearanceAudit {
        return ClearanceAudit(
            code = code,
            title = title,
            description = description,
            score = score,
            attributes = attributes,
            reviewer = reviewer,
            name = name,
            updatedAt = updatedAt,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a clearance audit in status proposed. */
fun newClearanceAudit(code: String, title: String): ClearanceAudit {
    return ClearanceAudit(
        code = code,
        title = title,
        description = "",
        score = 0.0,
        attributes = mutableMapOf<String, String>(),
        reviewer = "",
        name = "",
        updatedAt = 0,
        cost = 0.0,
        status = ClearanceAuditStatus.PROPOSED,
    )
}
