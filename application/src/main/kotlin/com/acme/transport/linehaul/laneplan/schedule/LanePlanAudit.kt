package com.acme.transport.linehaul.laneplan.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan audit is in its life cycle. */
enum class LanePlanAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lane plan audit. Its code is unique within a tenant. */
data class LanePlanAudit(
    var code: String,
    var label: String,
    var region: String,
    var attributes: MutableMap<String, String>,
    var summary: String,
    var revision: Int,
    var version: Int,
    var status: LanePlanAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanAuditStatus): Boolean {
        when (status) {
            LanePlanAuditStatus.IDLE -> {
                when (next) {
                    LanePlanAuditStatus.STOPPED -> return true
                }
                return false
            }
            LanePlanAuditStatus.WARMING -> return next == LanePlanAuditStatus.READY
            LanePlanAuditStatus.READY -> return next == LanePlanAuditStatus.DRAINING
            LanePlanAuditStatus.DRAINING -> {
                when (next) {
                    LanePlanAuditStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanAuditStatus) {
        if (!canMoveTo(next)) {
            error("lane plan audit $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"lane plan audit"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round(revision.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): LanePlanAudit {
        return LanePlanAudit(
            code = code,
            label = label,
            region = region,
            attributes = attributes,
            summary = summary,
            revision = revision,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a lane plan audit in status idle. */
fun newLanePlanAudit(code: String, label: String): LanePlanAudit {
    return LanePlanAudit(
        code = code,
        label = label,
        region = "",
        attributes = mutableMapOf<String, String>(),
        summary = "",
        revision = 0,
        version = 0,
        status = LanePlanAuditStatus.IDLE,
    )
}
