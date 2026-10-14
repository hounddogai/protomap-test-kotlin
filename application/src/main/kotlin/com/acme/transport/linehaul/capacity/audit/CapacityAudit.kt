package com.acme.transport.linehaul.capacity.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capacity audit records of linehaul's capacity work, and the rules, stores, and calculations around
// them.

/** Where a capacity audit is in its life cycle. */
enum class CapacityAuditStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One capacity audit, with what the planners need to act on it. */
data class CapacityAudit(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var capacity: Int,
    var checksum: String,
    var active: Boolean,
    var revision: Int,
    var etaMinutes: Int,
    var trailerId: String,
    var limit: Int,
    var status: CapacityAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityAuditStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityAuditStatus): Boolean {
        when (status) {
            CapacityAuditStatus.REQUESTED -> return next == CapacityAuditStatus.REVOKED || next == CapacityAuditStatus.EXPIRED
            CapacityAuditStatus.GRANTED -> {
                when (next) {
                    CapacityAuditStatus.REVOKED -> return true
                }
                return false
            }
            CapacityAuditStatus.REVOKED -> {
                when (next) {
                    CapacityAuditStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityAuditStatus) {
        if (!canMoveTo(next)) {
            error("capacity audit $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((limit.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): CapacityAudit {
        return CapacityAudit(
            code = code,
            title = title,
            attributes = attributes,
            capacity = capacity,
            checksum = checksum,
            active = active,
            revision = revision,
            etaMinutes = etaMinutes,
            trailerId = trailerId,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capacity audit in status requested. */
fun newCapacityAudit(code: String, title: String): CapacityAudit {
    return CapacityAudit(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        capacity = 0,
        checksum = "",
        active = false,
        revision = 0,
        etaMinutes = 0,
        trailerId = "",
        limit = 0,
        status = CapacityAuditStatus.REQUESTED,
    )
}
