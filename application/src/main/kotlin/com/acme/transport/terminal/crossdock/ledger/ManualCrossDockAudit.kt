package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual cross dock audit is in its life cycle. */
enum class ManualCrossDockAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One manual cross dock audit, with what the planners need to act on it. */
data class ManualCrossDockAudit(
    var code: String,
    var name: String,
    var active: Boolean,
    var attributes: MutableMap<String, String>,
    var vehicleId: String,
    var ratio: Double,
    var startsAt: Int,
    var cost: Double,
    var status: ManualCrossDockAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCrossDockAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCrossDockAuditStatus): Boolean {
        when (status) {
            ManualCrossDockAuditStatus.DRAFT -> return next == ManualCrossDockAuditStatus.PENDING || next == ManualCrossDockAuditStatus.ACTIVE
            ManualCrossDockAuditStatus.PENDING -> {
                when (next) {
                    ManualCrossDockAuditStatus.SUSPENDED -> return true
                }
                return false
            }
            ManualCrossDockAuditStatus.ACTIVE -> return next == ManualCrossDockAuditStatus.SUSPENDED
            ManualCrossDockAuditStatus.SUSPENDED -> {
                when (next) {
                    ManualCrossDockAuditStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualCrossDockAuditStatus) {
        if (!canMoveTo(next)) {
            error("manual cross dock audit $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round(ratio * cost * 100) / 100

    fun snapshot(): ManualCrossDockAudit {
        return ManualCrossDockAudit(
            code = code,
            name = name,
            active = active,
            attributes = attributes,
            vehicleId = vehicleId,
            ratio = ratio,
            startsAt = startsAt,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual cross dock audit in status draft. */
fun newManualCrossDockAudit(code: String, name: String): ManualCrossDockAudit {
    return ManualCrossDockAudit(
        code = code,
        name = name,
        active = false,
        attributes = mutableMapOf<String, String>(),
        vehicleId = "",
        ratio = 0.0,
        startsAt = 0,
        cost = 0.0,
        status = ManualCrossDockAuditStatus.DRAFT,
    )
}
