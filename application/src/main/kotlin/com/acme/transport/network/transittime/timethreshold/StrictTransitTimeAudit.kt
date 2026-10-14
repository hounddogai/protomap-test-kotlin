package com.acme.transport.network.transittime.timethreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict transit time audit is in its life cycle. */
enum class StrictTransitTimeAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A strict transit time audit. Its code is unique within a tenant. */
data class StrictTransitTimeAudit(
    var code: String,
    var name: String,
    var startsAt: Int,
    var priority: Int,
    var retries: Int,
    var carrier: String,
    var discount: Double,
    var ceiling: Double,
    var summary: String,
    var status: StrictTransitTimeAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTransitTimeAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTransitTimeAuditStatus): Boolean {
        when (status) {
            StrictTransitTimeAuditStatus.IDLE -> return next == StrictTransitTimeAuditStatus.STOPPED
            StrictTransitTimeAuditStatus.WARMING -> {
                when (next) {
                    StrictTransitTimeAuditStatus.DRAINING, StrictTransitTimeAuditStatus.STOPPED -> return true
                }
                return false
            }
            StrictTransitTimeAuditStatus.READY -> return next == StrictTransitTimeAuditStatus.DRAINING
            StrictTransitTimeAuditStatus.DRAINING -> return next == StrictTransitTimeAuditStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictTransitTimeAuditStatus) {
        if (!canMoveTo(next)) {
            error("strict transit time audit $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedExposure(): Double = round((startsAt.toDouble() - ceiling) * 100) / 100

    fun snapshot(): StrictTransitTimeAudit {
        return StrictTransitTimeAudit(
            code = code,
            name = name,
            startsAt = startsAt,
            priority = priority,
            retries = retries,
            carrier = carrier,
            discount = discount,
            ceiling = ceiling,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict transit time audit in status idle. */
fun newStrictTransitTimeAudit(code: String, name: String): StrictTransitTimeAudit {
    return StrictTransitTimeAudit(
        code = code,
        name = name,
        startsAt = 0,
        priority = 0,
        retries = 0,
        carrier = "",
        discount = 0.0,
        ceiling = 0.0,
        summary = "",
        status = StrictTransitTimeAuditStatus.IDLE,
    )
}
