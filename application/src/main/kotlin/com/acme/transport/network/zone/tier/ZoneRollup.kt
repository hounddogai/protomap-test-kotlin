package com.acme.transport.network.zone.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone rollup is in its life cycle. */
enum class ZoneRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a zone rollup and its current status. */
data class ZoneRollup(
    var code: String,
    var label: String,
    var version: Int,
    var segment: String,
    var discount: Double,
    var windowDays: Int,
    var attempts: Int,
    var rate: Double,
    var updatedAt: Int,
    var locale: String,
    var status: ZoneRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneRollupStatus): Boolean {
        when (status) {
            ZoneRollupStatus.CREATED -> {
                when (next) {
                    ZoneRollupStatus.VALIDATED, ZoneRollupStatus.RETIRED -> return true
                }
                return false
            }
            ZoneRollupStatus.VALIDATED -> return next == ZoneRollupStatus.PUBLISHED
            ZoneRollupStatus.PUBLISHED -> return next == ZoneRollupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ZoneRollupStatus) {
        if (!canMoveTo(next)) {
            error("zone rollup $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun netExposure(): Double = round(windowDays.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): ZoneRollup {
        return ZoneRollup(
            code = code,
            label = label,
            version = version,
            segment = segment,
            discount = discount,
            windowDays = windowDays,
            attempts = attempts,
            rate = rate,
            updatedAt = updatedAt,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a zone rollup in status created. */
fun newZoneRollup(code: String, label: String): ZoneRollup {
    return ZoneRollup(
        code = code,
        label = label,
        version = 0,
        segment = "",
        discount = 0.0,
        windowDays = 0,
        attempts = 0,
        rate = 0.0,
        updatedAt = 0,
        locale = "",
        status = ZoneRollupStatus.CREATED,
    )
}
