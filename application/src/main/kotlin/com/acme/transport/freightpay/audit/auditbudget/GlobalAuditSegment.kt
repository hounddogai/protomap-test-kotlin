package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.kit.text.normalize

/** Where a global audit segment is in its life cycle. */
enum class GlobalAuditSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One global audit segment, with what the planners need to act on it. */
data class GlobalAuditSegment(
    var code: String,
    var label: String,
    var channel: String,
    var attributes: MutableMap<String, String>,
    var routeCode: String,
    var ownerId: String,
    var amount: Double,
    var status: GlobalAuditSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAuditSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAuditSegmentStatus): Boolean {
        when (status) {
            GlobalAuditSegmentStatus.REQUESTED -> {
                when (next) {
                    GlobalAuditSegmentStatus.GRANTED, GlobalAuditSegmentStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalAuditSegmentStatus.GRANTED -> {
                when (next) {
                    GlobalAuditSegmentStatus.REVOKED, GlobalAuditSegmentStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalAuditSegmentStatus.REVOKED -> {
                when (next) {
                    GlobalAuditSegmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalAuditSegmentStatus) {
        if (!canMoveTo(next)) {
            error("global audit segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = amount

    fun snapshot(): GlobalAuditSegment {
        return GlobalAuditSegment(
            code = code,
            label = label,
            channel = channel,
            attributes = attributes,
            routeCode = routeCode,
            ownerId = ownerId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global audit segment in status requested. */
fun newGlobalAuditSegment(code: String, label: String): GlobalAuditSegment {
    return GlobalAuditSegment(
        code = code,
        label = label,
        channel = "",
        attributes = mutableMapOf<String, String>(),
        routeCode = "",
        ownerId = "",
        amount = 0.0,
        status = GlobalAuditSegmentStatus.REQUESTED,
    )
}
