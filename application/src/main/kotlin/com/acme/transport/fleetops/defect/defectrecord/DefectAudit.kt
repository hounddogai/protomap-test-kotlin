package com.acme.transport.fleetops.defect.defectrecord

import com.acme.transport.kit.text.normalize

/** Where a defect audit is in its life cycle. */
enum class DefectAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One defect audit, with what the planners need to act on it. */
data class DefectAudit(
    var code: String,
    var name: String,
    var timezone: String,
    var attributes: MutableMap<String, String>,
    var loadKg: Double,
    var phone: String,
    var trailerId: String,
    var routeCode: String,
    var status: DefectAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectAuditStatus): Boolean {
        when (status) {
            DefectAuditStatus.PLANNED -> {
                when (next) {
                    DefectAuditStatus.SCHEDULED, DefectAuditStatus.CANCELLED -> return true
                }
                return false
            }
            DefectAuditStatus.SCHEDULED -> {
                when (next) {
                    DefectAuditStatus.IN_PROGRESS -> return true
                }
                return false
            }
            DefectAuditStatus.IN_PROGRESS -> return next == DefectAuditStatus.COMPLETED
            DefectAuditStatus.COMPLETED -> return next == DefectAuditStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DefectAuditStatus) {
        if (!canMoveTo(next)) {
            error("defect audit $code cannot move from $status to $next")
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

    fun measuredValue(): Double = loadKg

    fun snapshot(): DefectAudit {
        return DefectAudit(
            code = code,
            name = name,
            timezone = timezone,
            attributes = attributes,
            loadKg = loadKg,
            phone = phone,
            trailerId = trailerId,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a defect audit in status planned. */
fun newDefectAudit(code: String, name: String): DefectAudit {
    return DefectAudit(
        code = code,
        name = name,
        timezone = "",
        attributes = mutableMapOf<String, String>(),
        loadKg = 0.0,
        phone = "",
        trailerId = "",
        routeCode = "",
        status = DefectAuditStatus.PLANNED,
    )
}
