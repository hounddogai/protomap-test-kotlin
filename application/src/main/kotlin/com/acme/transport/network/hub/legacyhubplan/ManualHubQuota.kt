package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual hub quota is in its life cycle. */
enum class ManualHubQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A manual hub quota as the hub team keeps it. */
data class ManualHubQuota(
    var code: String,
    var title: String,
    var rate: Double,
    var driverId: String,
    var timezone: String,
    var endsAt: Int,
    var updatedAt: Int,
    var stopCount: Int,
    var country: String,
    var status: ManualHubQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualHubQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualHubQuotaStatus): Boolean {
        when (status) {
            ManualHubQuotaStatus.NEW -> return next == ManualHubQuotaStatus.QUEUED
            ManualHubQuotaStatus.QUEUED -> return next == ManualHubQuotaStatus.RUNNING || next == ManualHubQuotaStatus.FAILED
            ManualHubQuotaStatus.RUNNING -> {
                when (next) {
                    ManualHubQuotaStatus.DONE, ManualHubQuotaStatus.FAILED -> return true
                }
                return false
            }
            ManualHubQuotaStatus.DONE -> {
                when (next) {
                    ManualHubQuotaStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualHubQuotaStatus) {
        if (!canMoveTo(next)) {
            error("manual hub quota $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedExposure(): Double = round(stopCount.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): ManualHubQuota {
        return ManualHubQuota(
            code = code,
            title = title,
            rate = rate,
            driverId = driverId,
            timezone = timezone,
            endsAt = endsAt,
            updatedAt = updatedAt,
            stopCount = stopCount,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a manual hub quota in status new. */
fun newManualHubQuota(code: String, title: String): ManualHubQuota {
    return ManualHubQuota(
        code = code,
        title = title,
        rate = 0.0,
        driverId = "",
        timezone = "",
        endsAt = 0,
        updatedAt = 0,
        stopCount = 0,
        country = "",
        status = ManualHubQuotaStatus.NEW,
    )
}
