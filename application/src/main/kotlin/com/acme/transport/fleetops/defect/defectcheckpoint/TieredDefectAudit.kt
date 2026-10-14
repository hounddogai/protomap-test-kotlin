package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered defect audit is in its life cycle. */
enum class TieredDefectAuditStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered defect audit as the defect team keeps it. */
data class TieredDefectAudit(
    var code: String,
    var title: String,
    var ratio: Double,
    var threshold: Double,
    var ownerId: String,
    var pinned: Boolean,
    var expiresAt: Int,
    var windowDays: Int,
    var category: String,
    var attributes: MutableMap<String, String>,
    var status: TieredDefectAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDefectAuditStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDefectAuditStatus): Boolean {
        when (status) {
            TieredDefectAuditStatus.PROPOSED -> {
                when (next) {
                    TieredDefectAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredDefectAuditStatus.APPROVED -> return next == TieredDefectAuditStatus.WITHDRAWN
            TieredDefectAuditStatus.REJECTED -> {
                when (next) {
                    TieredDefectAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDefectAuditStatus) {
        if (!canMoveTo(next)) {
            error("tiered defect audit $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round(windowDays.toDouble() * ratio * 100) / 100

    fun snapshot(): TieredDefectAudit {
        return TieredDefectAudit(
            code = code,
            title = title,
            ratio = ratio,
            threshold = threshold,
            ownerId = ownerId,
            pinned = pinned,
            expiresAt = expiresAt,
            windowDays = windowDays,
            category = category,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tiered defect audit in status proposed. */
fun newTieredDefectAudit(code: String, title: String): TieredDefectAudit {
    return TieredDefectAudit(
        code = code,
        title = title,
        ratio = 0.0,
        threshold = 0.0,
        ownerId = "",
        pinned = false,
        expiresAt = 0,
        windowDays = 0,
        category = "",
        attributes = mutableMapOf<String, String>(),
        status = TieredDefectAuditStatus.PROPOSED,
    )
}
