package com.acme.transport.fleetops.inspection.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection audit is in its life cycle. */
enum class InspectionAuditStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A inspection audit as the inspection team keeps it. */
data class InspectionAudit(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var carrier: String,
    var score: Double,
    var ownerId: String,
    var timezone: String,
    var attributes: MutableMap<String, String>,
    var durationMinutes: Int,
    var pinned: Boolean,
    var status: InspectionAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionAuditStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionAuditStatus): Boolean {
        when (status) {
            InspectionAuditStatus.REQUESTED -> return next == InspectionAuditStatus.REVOKED || next == InspectionAuditStatus.EXPIRED
            InspectionAuditStatus.GRANTED -> {
                when (next) {
                    InspectionAuditStatus.REVOKED, InspectionAuditStatus.EXPIRED -> return true
                }
                return false
            }
            InspectionAuditStatus.REVOKED -> {
                when (next) {
                    InspectionAuditStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionAuditStatus) {
        if (!canMoveTo(next)) {
            error("inspection audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round(durationMinutes.toDouble() * score * 100) / 100

    fun snapshot(): InspectionAudit {
        return InspectionAudit(
            code = code,
            title = title,
            tags = tags,
            carrier = carrier,
            score = score,
            ownerId = ownerId,
            timezone = timezone,
            attributes = attributes,
            durationMinutes = durationMinutes,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection audit in status requested. */
fun newInspectionAudit(code: String, title: String): InspectionAudit {
    return InspectionAudit(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        carrier = "",
        score = 0.0,
        ownerId = "",
        timezone = "",
        attributes = mutableMapOf<String, String>(),
        durationMinutes = 0,
        pinned = false,
        status = InspectionAuditStatus.REQUESTED,
    )
}
