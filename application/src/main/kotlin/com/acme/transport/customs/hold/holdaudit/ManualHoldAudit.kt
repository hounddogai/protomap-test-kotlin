package com.acme.transport.customs.hold.holdaudit

import com.acme.transport.kit.text.normalize

// The manual hold audit records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a manual hold audit is in its life cycle. */
enum class ManualHoldAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One manual hold audit, with what the planners need to act on it. */
data class ManualHoldAudit(
    var code: String,
    var name: String,
    var summary: String,
    var approved: Boolean,
    var category: String,
    var contactEmail: String,
    var attributes: MutableMap<String, String>,
    var quantity: Int,
    var status: ManualHoldAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualHoldAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualHoldAuditStatus): Boolean {
        when (status) {
            ManualHoldAuditStatus.PLANNED -> {
                when (next) {
                    ManualHoldAuditStatus.IN_PROGRESS, ManualHoldAuditStatus.CANCELLED -> return true
                }
                return false
            }
            ManualHoldAuditStatus.SCHEDULED -> {
                when (next) {
                    ManualHoldAuditStatus.COMPLETED -> return true
                }
                return false
            }
            ManualHoldAuditStatus.IN_PROGRESS -> {
                when (next) {
                    ManualHoldAuditStatus.COMPLETED, ManualHoldAuditStatus.CANCELLED -> return true
                }
                return false
            }
            ManualHoldAuditStatus.COMPLETED -> {
                when (next) {
                    ManualHoldAuditStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualHoldAuditStatus) {
        if (!canMoveTo(next)) {
            error("manual hold audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): ManualHoldAudit {
        return ManualHoldAudit(
            code = code,
            name = name,
            summary = summary,
            approved = approved,
            category = category,
            contactEmail = contactEmail,
            attributes = attributes,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a manual hold audit in status planned. */
fun newManualHoldAudit(code: String, name: String): ManualHoldAudit {
    return ManualHoldAudit(
        code = code,
        name = name,
        summary = "",
        approved = false,
        category = "",
        contactEmail = "",
        attributes = mutableMapOf<String, String>(),
        quantity = 0,
        status = ManualHoldAuditStatus.PLANNED,
    )
}
