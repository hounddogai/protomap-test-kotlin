package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute audit is in its life cycle. */
enum class DisputeAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A dispute audit. Its code is unique within a tenant. */
data class DisputeAudit(
    var code: String,
    var name: String,
    var sequence: Int,
    var minimum: Double,
    var attributes: MutableMap<String, String>,
    var distanceKm: Double,
    var discount: Double,
    var status: DisputeAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeAuditStatus): Boolean {
        when (status) {
            DisputeAuditStatus.OPEN -> return next == DisputeAuditStatus.IN_REVIEW || next == DisputeAuditStatus.ARCHIVED
            DisputeAuditStatus.IN_REVIEW -> {
                when (next) {
                    DisputeAuditStatus.REOPENED -> return true
                }
                return false
            }
            DisputeAuditStatus.RESOLVED -> return next == DisputeAuditStatus.REOPENED || next == DisputeAuditStatus.ARCHIVED
            DisputeAuditStatus.REOPENED -> return next == DisputeAuditStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DisputeAuditStatus) {
        if (!canMoveTo(next)) {
            error("dispute audit $code cannot move from $status to $next")
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

    fun weightedValue(): Double = round((discount - distanceKm) * 100) / 100

    fun snapshot(): DisputeAudit {
        return DisputeAudit(
            code = code,
            name = name,
            sequence = sequence,
            minimum = minimum,
            attributes = attributes,
            distanceKm = distanceKm,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dispute audit in status open. */
fun newDisputeAudit(code: String, name: String): DisputeAudit {
    return DisputeAudit(
        code = code,
        name = name,
        sequence = 0,
        minimum = 0.0,
        attributes = mutableMapOf<String, String>(),
        distanceKm = 0.0,
        discount = 0.0,
        status = DisputeAuditStatus.OPEN,
    )
}
