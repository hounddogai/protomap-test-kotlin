package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional near miss audit is in its life cycle. */
enum class RegionalNearMissAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a regional near miss audit and its current status. */
data class RegionalNearMissAudit(
    var code: String,
    var name: String,
    var cost: Double,
    var rate: Double,
    var stopCount: Int,
    var shiftCode: String,
    var limit: Int,
    var depot: String,
    var unit: String,
    var status: RegionalNearMissAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalNearMissAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalNearMissAuditStatus): Boolean {
        when (status) {
            RegionalNearMissAuditStatus.IDLE -> {
                when (next) {
                    RegionalNearMissAuditStatus.WARMING -> return true
                }
                return false
            }
            RegionalNearMissAuditStatus.WARMING -> {
                when (next) {
                    RegionalNearMissAuditStatus.READY -> return true
                }
                return false
            }
            RegionalNearMissAuditStatus.READY -> {
                when (next) {
                    RegionalNearMissAuditStatus.DRAINING, RegionalNearMissAuditStatus.STOPPED -> return true
                }
                return false
            }
            RegionalNearMissAuditStatus.DRAINING -> return next == RegionalNearMissAuditStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RegionalNearMissAuditStatus) {
        if (!canMoveTo(next)) {
            error("regional near miss audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedVolume(): Double = round((cost + rate) * 100) / 100

    fun snapshot(): RegionalNearMissAudit {
        return RegionalNearMissAudit(
            code = code,
            name = name,
            cost = cost,
            rate = rate,
            stopCount = stopCount,
            shiftCode = shiftCode,
            limit = limit,
            depot = depot,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a regional near miss audit in status idle. */
fun newRegionalNearMissAudit(code: String, name: String): RegionalNearMissAudit {
    return RegionalNearMissAudit(
        code = code,
        name = name,
        cost = 0.0,
        rate = 0.0,
        stopCount = 0,
        shiftCode = "",
        limit = 0,
        depot = "",
        unit = "",
        status = RegionalNearMissAuditStatus.IDLE,
    )
}
