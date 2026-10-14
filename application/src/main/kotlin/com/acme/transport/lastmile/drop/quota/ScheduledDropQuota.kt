package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.text.normalize

// The scheduled drop quota records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a scheduled drop quota is in its life cycle. */
enum class ScheduledDropQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a scheduled drop quota and its current status. */
data class ScheduledDropQuota(
    var code: String,
    var label: String,
    var title: String,
    var region: String,
    var carrier: String,
    var contactEmail: String,
    var limit: Int,
    var status: ScheduledDropQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDropQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDropQuotaStatus): Boolean {
        when (status) {
            ScheduledDropQuotaStatus.PROPOSED -> {
                when (next) {
                    ScheduledDropQuotaStatus.REJECTED, ScheduledDropQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScheduledDropQuotaStatus.APPROVED -> {
                when (next) {
                    ScheduledDropQuotaStatus.REJECTED, ScheduledDropQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScheduledDropQuotaStatus.REJECTED -> return next == ScheduledDropQuotaStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ScheduledDropQuotaStatus) {
        if (!canMoveTo(next)) {
            error("scheduled drop quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): ScheduledDropQuota {
        return ScheduledDropQuota(
            code = code,
            label = label,
            title = title,
            region = region,
            carrier = carrier,
            contactEmail = contactEmail,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled drop quota in status proposed. */
fun newScheduledDropQuota(code: String, label: String): ScheduledDropQuota {
    return ScheduledDropQuota(
        code = code,
        label = label,
        title = "",
        region = "",
        carrier = "",
        contactEmail = "",
        limit = 0,
        status = ScheduledDropQuotaStatus.PROPOSED,
    )
}
