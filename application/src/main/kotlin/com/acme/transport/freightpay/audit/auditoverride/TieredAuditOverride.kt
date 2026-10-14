package com.acme.transport.freightpay.audit.auditoverride

import com.acme.transport.kit.text.normalize

// The tiered audit override records of freightpay's audit work, and the rules, stores, and calculations
// around them.

/** Where a tiered audit override is in its life cycle. */
enum class TieredAuditOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tiered audit override, with what the planners need to act on it. */
data class TieredAuditOverride(
    var code: String,
    var title: String,
    var timezone: String,
    var summary: String,
    var createdAt: Int,
    var status: TieredAuditOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAuditOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAuditOverrideStatus): Boolean {
        when (status) {
            TieredAuditOverrideStatus.IDLE -> return next == TieredAuditOverrideStatus.WARMING || next == TieredAuditOverrideStatus.STOPPED
            TieredAuditOverrideStatus.WARMING -> {
                when (next) {
                    TieredAuditOverrideStatus.READY -> return true
                }
                return false
            }
            TieredAuditOverrideStatus.READY -> {
                when (next) {
                    TieredAuditOverrideStatus.DRAINING -> return true
                }
                return false
            }
            TieredAuditOverrideStatus.DRAINING -> return next == TieredAuditOverrideStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TieredAuditOverrideStatus) {
        if (!canMoveTo(next)) {
            error("tiered audit override $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): TieredAuditOverride {
        return TieredAuditOverride(
            code = code,
            title = title,
            timezone = timezone,
            summary = summary,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered audit override in status idle. */
fun newTieredAuditOverride(code: String, title: String): TieredAuditOverride {
    return TieredAuditOverride(
        code = code,
        title = title,
        timezone = "",
        summary = "",
        createdAt = 0,
        status = TieredAuditOverrideStatus.IDLE,
    )
}
