package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.kit.text.normalize

/** Where a manual backhaul audit is in its life cycle. */
enum class ManualBackhaulAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One manual backhaul audit, with what the planners need to act on it. */
data class ManualBackhaulAudit(
    var code: String,
    var label: String,
    var region: String,
    var revision: Int,
    var approved: Boolean,
    var summary: String,
    var description: String,
    var active: Boolean,
    var attributes: MutableMap<String, String>,
    var status: ManualBackhaulAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualBackhaulAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualBackhaulAuditStatus): Boolean {
        when (status) {
            ManualBackhaulAuditStatus.PLANNED -> {
                when (next) {
                    ManualBackhaulAuditStatus.SCHEDULED, ManualBackhaulAuditStatus.COMPLETED -> return true
                }
                return false
            }
            ManualBackhaulAuditStatus.SCHEDULED -> return next == ManualBackhaulAuditStatus.COMPLETED
            ManualBackhaulAuditStatus.IN_PROGRESS -> {
                when (next) {
                    ManualBackhaulAuditStatus.COMPLETED -> return true
                }
                return false
            }
            ManualBackhaulAuditStatus.COMPLETED -> return next == ManualBackhaulAuditStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ManualBackhaulAuditStatus) {
        if (!canMoveTo(next)) {
            error("manual backhaul audit $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"manual backhaul audit"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): ManualBackhaulAudit {
        return ManualBackhaulAudit(
            code = code,
            label = label,
            region = region,
            revision = revision,
            approved = approved,
            summary = summary,
            description = description,
            active = active,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a manual backhaul audit in status planned. */
fun newManualBackhaulAudit(code: String, label: String): ManualBackhaulAudit {
    return ManualBackhaulAudit(
        code = code,
        label = label,
        region = "",
        revision = 0,
        approved = false,
        summary = "",
        description = "",
        active = false,
        attributes = mutableMapOf<String, String>(),
        status = ManualBackhaulAuditStatus.PLANNED,
    )
}
