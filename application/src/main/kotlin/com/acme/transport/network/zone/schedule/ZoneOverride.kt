package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone override is in its life cycle. */
enum class ZoneOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A zone override. Its code is unique within a tenant. */
data class ZoneOverride(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var reviewer: String,
    var distanceKm: Double,
    var expiresAt: Int,
    var title: String,
    var status: ZoneOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneOverrideStatus): Boolean {
        when (status) {
            ZoneOverrideStatus.OPEN -> {
                when (next) {
                    ZoneOverrideStatus.IN_REVIEW, ZoneOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            ZoneOverrideStatus.IN_REVIEW -> return next == ZoneOverrideStatus.RESOLVED || next == ZoneOverrideStatus.ARCHIVED
            ZoneOverrideStatus.RESOLVED -> {
                when (next) {
                    ZoneOverrideStatus.REOPENED, ZoneOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            ZoneOverrideStatus.REOPENED -> return next == ZoneOverrideStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ZoneOverrideStatus) {
        if (!canMoveTo(next)) {
            error("zone override $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedLoad(): Double = round((expiresAt.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): ZoneOverride {
        return ZoneOverride(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            reviewer = reviewer,
            distanceKm = distanceKm,
            expiresAt = expiresAt,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a zone override in status open. */
fun newZoneOverride(code: String, label: String): ZoneOverride {
    return ZoneOverride(
        code = code,
        label = label,
        etaMinutes = 0,
        reviewer = "",
        distanceKm = 0.0,
        expiresAt = 0,
        title = "",
        status = ZoneOverrideStatus.OPEN,
    )
}
