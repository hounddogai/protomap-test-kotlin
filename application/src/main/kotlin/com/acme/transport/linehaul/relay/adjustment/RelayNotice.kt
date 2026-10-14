package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay notice is in its life cycle. */
enum class RelayNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A relay notice as the relay team keeps it. */
data class RelayNotice(
    var code: String,
    var label: String,
    var ceiling: Double,
    var locale: String,
    var odometer: Int,
    var distanceKm: Double,
    var retries: Int,
    var limit: Int,
    var timezone: String,
    var category: String,
    var checksum: String,
    var status: RelayNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayNoticeStatus): Boolean {
        when (status) {
            RelayNoticeStatus.IDLE -> {
                when (next) {
                    RelayNoticeStatus.READY, RelayNoticeStatus.DRAINING -> return true
                }
                return false
            }
            RelayNoticeStatus.WARMING -> {
                when (next) {
                    RelayNoticeStatus.READY -> return true
                }
                return false
            }
            RelayNoticeStatus.READY -> return next == RelayNoticeStatus.DRAINING || next == RelayNoticeStatus.STOPPED
            RelayNoticeStatus.DRAINING -> return next == RelayNoticeStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RelayNoticeStatus) {
        if (!canMoveTo(next)) {
            error("relay notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedLoad(): Double = round((limit.toDouble() - ceiling) * 100) / 100

    fun snapshot(): RelayNotice {
        return RelayNotice(
            code = code,
            label = label,
            ceiling = ceiling,
            locale = locale,
            odometer = odometer,
            distanceKm = distanceKm,
            retries = retries,
            limit = limit,
            timezone = timezone,
            category = category,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a relay notice in status idle. */
fun newRelayNotice(code: String, label: String): RelayNotice {
    return RelayNotice(
        code = code,
        label = label,
        ceiling = 0.0,
        locale = "",
        odometer = 0,
        distanceKm = 0.0,
        retries = 0,
        limit = 0,
        timezone = "",
        category = "",
        checksum = "",
        status = RelayNoticeStatus.IDLE,
    )
}
