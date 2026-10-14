package com.acme.transport.network.zone.zonetier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone score is in its life cycle. */
enum class ZoneScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A zone score as the zone team keeps it. */
data class ZoneScore(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var region: String,
    var maximum: Double,
    var currency: String,
    var channel: String,
    var timezone: String,
    var updatedAt: Int,
    var status: ZoneScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneScoreStatus): Boolean {
        when (status) {
            ZoneScoreStatus.NEW -> {
                when (next) {
                    ZoneScoreStatus.RUNNING, ZoneScoreStatus.DONE -> return true
                }
                return false
            }
            ZoneScoreStatus.QUEUED -> {
                when (next) {
                    ZoneScoreStatus.RUNNING, ZoneScoreStatus.FAILED -> return true
                }
                return false
            }
            ZoneScoreStatus.RUNNING -> {
                when (next) {
                    ZoneScoreStatus.DONE, ZoneScoreStatus.FAILED -> return true
                }
                return false
            }
            ZoneScoreStatus.DONE -> {
                when (next) {
                    ZoneScoreStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneScoreStatus) {
        if (!canMoveTo(next)) {
            error("zone score $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"zone score"} $code"

    fun adjustedVolume(): Double = round(maximum * updatedAt.toDouble() * 100) / 100

    fun snapshot(): ZoneScore {
        return ZoneScore(
            code = code,
            title = title,
            pinned = pinned,
            region = region,
            maximum = maximum,
            currency = currency,
            channel = channel,
            timezone = timezone,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a zone score in status new. */
fun newZoneScore(code: String, title: String): ZoneScore {
    return ZoneScore(
        code = code,
        title = title,
        pinned = false,
        region = "",
        maximum = 0.0,
        currency = "",
        channel = "",
        timezone = "",
        updatedAt = 0,
        status = ZoneScoreStatus.NEW,
    )
}
