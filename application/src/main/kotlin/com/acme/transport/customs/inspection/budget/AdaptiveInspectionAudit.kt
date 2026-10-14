package com.acme.transport.customs.inspection.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive inspection audit is in its life cycle. */
enum class AdaptiveInspectionAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a adaptive inspection audit and its current status. */
data class AdaptiveInspectionAudit(
    var code: String,
    var name: String,
    var timezone: String,
    var updatedAt: Int,
    var stopCount: Int,
    var etaMinutes: Int,
    var status: AdaptiveInspectionAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionAuditStatus): Boolean {
        when (status) {
            AdaptiveInspectionAuditStatus.IDLE -> {
                when (next) {
                    AdaptiveInspectionAuditStatus.WARMING -> return true
                }
                return false
            }
            AdaptiveInspectionAuditStatus.WARMING -> {
                when (next) {
                    AdaptiveInspectionAuditStatus.DRAINING, AdaptiveInspectionAuditStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveInspectionAuditStatus.READY -> return next == AdaptiveInspectionAuditStatus.DRAINING
            AdaptiveInspectionAuditStatus.DRAINING -> return next == AdaptiveInspectionAuditStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionAuditStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection audit $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netLoad(): Double = round(etaMinutes.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): AdaptiveInspectionAudit {
        return AdaptiveInspectionAudit(
            code = code,
            name = name,
            timezone = timezone,
            updatedAt = updatedAt,
            stopCount = stopCount,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive inspection audit in status idle. */
fun newAdaptiveInspectionAudit(code: String, name: String): AdaptiveInspectionAudit {
    return AdaptiveInspectionAudit(
        code = code,
        name = name,
        timezone = "",
        updatedAt = 0,
        stopCount = 0,
        etaMinutes = 0,
        status = AdaptiveInspectionAuditStatus.IDLE,
    )
}
