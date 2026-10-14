package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The backhaul audit records of linehaul's backhaul work, and the rules, stores, and calculations around
// them.

/** Where a backhaul audit is in its life cycle. */
enum class BackhaulAuditStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a backhaul audit and its current status. */
data class BackhaulAudit(
    var code: String,
    var label: String,
    var channel: String,
    var margin: Double,
    var expiresAt: Int,
    var distanceKm: Double,
    var fuelLiters: Double,
    var tags: MutableList<String>,
    var sequence: Int,
    var status: BackhaulAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulAuditStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulAuditStatus): Boolean {
        when (status) {
            BackhaulAuditStatus.PROPOSED -> {
                when (next) {
                    BackhaulAuditStatus.APPROVED, BackhaulAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
            BackhaulAuditStatus.APPROVED -> {
                when (next) {
                    BackhaulAuditStatus.REJECTED -> return true
                }
                return false
            }
            BackhaulAuditStatus.REJECTED -> return next == BackhaulAuditStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BackhaulAuditStatus) {
        if (!canMoveTo(next)) {
            error("backhaul audit $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"backhaul audit"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveVolume(): Double = round(distanceKm * expiresAt.toDouble() * 100) / 100

    fun snapshot(): BackhaulAudit {
        return BackhaulAudit(
            code = code,
            label = label,
            channel = channel,
            margin = margin,
            expiresAt = expiresAt,
            distanceKm = distanceKm,
            fuelLiters = fuelLiters,
            tags = tags,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a backhaul audit in status proposed. */
fun newBackhaulAudit(code: String, label: String): BackhaulAudit {
    return BackhaulAudit(
        code = code,
        label = label,
        channel = "",
        margin = 0.0,
        expiresAt = 0,
        distanceKm = 0.0,
        fuelLiters = 0.0,
        tags = mutableListOf<String>(),
        sequence = 0,
        status = BackhaulAuditStatus.PROPOSED,
    )
}
