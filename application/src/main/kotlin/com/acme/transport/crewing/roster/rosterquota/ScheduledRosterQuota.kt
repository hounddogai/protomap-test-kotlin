package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled roster quota records of crewing's roster work, and the rules, stores, and calculations around
// them.

/** Where a scheduled roster quota is in its life cycle. */
enum class ScheduledRosterQuotaStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled roster quota. Its code is unique within a tenant. */
data class ScheduledRosterQuota(
    var code: String,
    var title: String,
    var cost: Double,
    var driverId: String,
    var ratio: Double,
    var status: ScheduledRosterQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRosterQuotaStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRosterQuotaStatus): Boolean {
        when (status) {
            ScheduledRosterQuotaStatus.CREATED -> return next == ScheduledRosterQuotaStatus.PUBLISHED || next == ScheduledRosterQuotaStatus.RETIRED
            ScheduledRosterQuotaStatus.VALIDATED -> return next == ScheduledRosterQuotaStatus.PUBLISHED || next == ScheduledRosterQuotaStatus.RETIRED
            ScheduledRosterQuotaStatus.PUBLISHED -> return next == ScheduledRosterQuotaStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledRosterQuotaStatus) {
        if (!canMoveTo(next)) {
            error("scheduled roster quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled roster quota"} $code"

    fun projectedExposure(): Double = round((ratio - cost) * 100) / 100

    fun snapshot(): ScheduledRosterQuota {
        return ScheduledRosterQuota(
            code = code,
            title = title,
            cost = cost,
            driverId = driverId,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled roster quota in status created. */
fun newScheduledRosterQuota(code: String, title: String): ScheduledRosterQuota {
    return ScheduledRosterQuota(
        code = code,
        title = title,
        cost = 0.0,
        driverId = "",
        ratio = 0.0,
        status = ScheduledRosterQuotaStatus.CREATED,
    )
}
