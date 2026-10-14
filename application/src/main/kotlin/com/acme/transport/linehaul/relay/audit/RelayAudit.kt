package com.acme.transport.linehaul.relay.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The relay audit records of linehaul's relay work, and the rules, stores, and calculations around them.

/** Where a relay audit is in its life cycle. */
enum class RelayAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a relay audit and its current status. */
data class RelayAudit(
    var code: String,
    var name: String,
    var version: Int,
    var endsAt: Int,
    var budget: Double,
    var reviewer: String,
    var maximum: Double,
    var capacity: Int,
    var quantity: Int,
    var status: RelayAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayAuditStatus): Boolean {
        when (status) {
            RelayAuditStatus.OPEN -> {
                when (next) {
                    RelayAuditStatus.RESOLVED -> return true
                }
                return false
            }
            RelayAuditStatus.IN_REVIEW -> {
                when (next) {
                    RelayAuditStatus.RESOLVED, RelayAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            RelayAuditStatus.RESOLVED -> {
                when (next) {
                    RelayAuditStatus.REOPENED, RelayAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            RelayAuditStatus.REOPENED -> return next == RelayAuditStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RelayAuditStatus) {
        if (!canMoveTo(next)) {
            error("relay audit $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $name"

    fun projectedTotal(): Double = round(version.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): RelayAudit {
        return RelayAudit(
            code = code,
            name = name,
            version = version,
            endsAt = endsAt,
            budget = budget,
            reviewer = reviewer,
            maximum = maximum,
            capacity = capacity,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a relay audit in status open. */
fun newRelayAudit(code: String, name: String): RelayAudit {
    return RelayAudit(
        code = code,
        name = name,
        version = 0,
        endsAt = 0,
        budget = 0.0,
        reviewer = "",
        maximum = 0.0,
        capacity = 0,
        quantity = 0,
        status = RelayAuditStatus.OPEN,
    )
}
