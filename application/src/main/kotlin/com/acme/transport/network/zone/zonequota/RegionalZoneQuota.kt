package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional zone quota records of network's zone work, and the rules, stores, and calculations around
// them.

/** Where a regional zone quota is in its life cycle. */
enum class RegionalZoneQuotaStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One regional zone quota, with what the planners need to act on it. */
data class RegionalZoneQuota(
    var code: String,
    var name: String,
    var ratio: Double,
    var durationMinutes: Int,
    var revision: Int,
    var attempts: Int,
    var stopCount: Int,
    var status: RegionalZoneQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalZoneQuotaStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalZoneQuotaStatus): Boolean {
        when (status) {
            RegionalZoneQuotaStatus.CREATED -> {
                when (next) {
                    RegionalZoneQuotaStatus.PUBLISHED -> return true
                }
                return false
            }
            RegionalZoneQuotaStatus.VALIDATED -> return next == RegionalZoneQuotaStatus.PUBLISHED || next == RegionalZoneQuotaStatus.RETIRED
            RegionalZoneQuotaStatus.PUBLISHED -> {
                when (next) {
                    RegionalZoneQuotaStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalZoneQuotaStatus) {
        if (!canMoveTo(next)) {
            error("regional zone quota $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun weightedTotal(): Double = round((stopCount.toDouble() + ratio) * 100) / 100

    fun snapshot(): RegionalZoneQuota {
        return RegionalZoneQuota(
            code = code,
            name = name,
            ratio = ratio,
            durationMinutes = durationMinutes,
            revision = revision,
            attempts = attempts,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional zone quota in status created. */
fun newRegionalZoneQuota(code: String, name: String): RegionalZoneQuota {
    return RegionalZoneQuota(
        code = code,
        name = name,
        ratio = 0.0,
        durationMinutes = 0,
        revision = 0,
        attempts = 0,
        stopCount = 0,
        status = RegionalZoneQuotaStatus.CREATED,
    )
}
