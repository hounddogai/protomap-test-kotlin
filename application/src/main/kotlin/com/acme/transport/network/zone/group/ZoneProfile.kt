package com.acme.transport.network.zone.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone profile is in its life cycle. */
enum class ZoneProfileStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A zone profile. Its code is unique within a tenant. */
data class ZoneProfile(
    var code: String,
    var title: String,
    var depot: String,
    var batchSize: Int,
    var weight: Double,
    var threshold: Double,
    var version: Int,
    var status: ZoneProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneProfileStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneProfileStatus): Boolean {
        when (status) {
            ZoneProfileStatus.IDLE -> {
                when (next) {
                    ZoneProfileStatus.READY, ZoneProfileStatus.STOPPED -> return true
                }
                return false
            }
            ZoneProfileStatus.WARMING -> {
                when (next) {
                    ZoneProfileStatus.DRAINING -> return true
                }
                return false
            }
            ZoneProfileStatus.READY -> {
                when (next) {
                    ZoneProfileStatus.DRAINING, ZoneProfileStatus.STOPPED -> return true
                }
                return false
            }
            ZoneProfileStatus.DRAINING -> return next == ZoneProfileStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ZoneProfileStatus) {
        if (!canMoveTo(next)) {
            error("zone profile $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round((threshold - version.toDouble()) * 100) / 100

    fun snapshot(): ZoneProfile {
        return ZoneProfile(
            code = code,
            title = title,
            depot = depot,
            batchSize = batchSize,
            weight = weight,
            threshold = threshold,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a zone profile in status idle. */
fun newZoneProfile(code: String, title: String): ZoneProfile {
    return ZoneProfile(
        code = code,
        title = title,
        depot = "",
        batchSize = 0,
        weight = 0.0,
        threshold = 0.0,
        version = 0,
        status = ZoneProfileStatus.IDLE,
    )
}
