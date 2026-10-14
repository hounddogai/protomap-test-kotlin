package com.acme.transport.crewing.roster.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The roster quota records of crewing's roster work, and the rules, stores, and calculations around them.

/** Where a roster quota is in its life cycle. */
enum class RosterQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A roster quota. Its code is unique within a tenant. */
data class RosterQuota(
    var code: String,
    var title: String,
    var floorValue: Double,
    var attributes: MutableMap<String, String>,
    var visible: Boolean,
    var timezone: String,
    var dock: String,
    var maximum: Double,
    var priority: Int,
    var cost: Double,
    var status: RosterQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterQuotaStatus): Boolean {
        when (status) {
            RosterQuotaStatus.REQUESTED -> {
                when (next) {
                    RosterQuotaStatus.REVOKED, RosterQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            RosterQuotaStatus.GRANTED -> return next == RosterQuotaStatus.REVOKED || next == RosterQuotaStatus.EXPIRED
            RosterQuotaStatus.REVOKED -> {
                when (next) {
                    RosterQuotaStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterQuotaStatus) {
        if (!canMoveTo(next)) {
            error("roster quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"roster quota"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((floorValue - maximum) * 100) / 100

    fun snapshot(): RosterQuota {
        return RosterQuota(
            code = code,
            title = title,
            floorValue = floorValue,
            attributes = attributes,
            visible = visible,
            timezone = timezone,
            dock = dock,
            maximum = maximum,
            priority = priority,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a roster quota in status requested. */
fun newRosterQuota(code: String, title: String): RosterQuota {
    return RosterQuota(
        code = code,
        title = title,
        floorValue = 0.0,
        attributes = mutableMapOf<String, String>(),
        visible = false,
        timezone = "",
        dock = "",
        maximum = 0.0,
        priority = 0,
        cost = 0.0,
        status = RosterQuotaStatus.REQUESTED,
    )
}
