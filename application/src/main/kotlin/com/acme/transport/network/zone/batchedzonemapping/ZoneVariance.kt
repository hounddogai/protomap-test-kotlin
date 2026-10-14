package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone variance is in its life cycle. */
enum class ZoneVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A zone variance as the zone team keeps it. */
data class ZoneVariance(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var margin: Double,
    var retries: Int,
    var fuelLiters: Double,
    var timezone: String,
    var attempts: Int,
    var region: String,
    var capacity: Int,
    var description: String,
    var status: ZoneVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneVarianceStatus): Boolean {
        when (status) {
            ZoneVarianceStatus.OPEN -> {
                when (next) {
                    ZoneVarianceStatus.RESOLVED, ZoneVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
            ZoneVarianceStatus.IN_REVIEW -> {
                when (next) {
                    ZoneVarianceStatus.RESOLVED, ZoneVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
            ZoneVarianceStatus.RESOLVED -> return next == ZoneVarianceStatus.REOPENED || next == ZoneVarianceStatus.ARCHIVED
            ZoneVarianceStatus.REOPENED -> {
                when (next) {
                    ZoneVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneVarianceStatus) {
        if (!canMoveTo(next)) {
            error("zone variance $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"zone variance"} $code"

    fun projectedValue(): Double = round((margin + capacity.toDouble()) * 100) / 100

    fun snapshot(): ZoneVariance {
        return ZoneVariance(
            code = code,
            label = label,
            updatedAt = updatedAt,
            margin = margin,
            retries = retries,
            fuelLiters = fuelLiters,
            timezone = timezone,
            attempts = attempts,
            region = region,
            capacity = capacity,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a zone variance in status open. */
fun newZoneVariance(code: String, label: String): ZoneVariance {
    return ZoneVariance(
        code = code,
        label = label,
        updatedAt = 0,
        margin = 0.0,
        retries = 0,
        fuelLiters = 0.0,
        timezone = "",
        attempts = 0,
        region = "",
        capacity = 0,
        description = "",
        status = ZoneVarianceStatus.OPEN,
    )
}
