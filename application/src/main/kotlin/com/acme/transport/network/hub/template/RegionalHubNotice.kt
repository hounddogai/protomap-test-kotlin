package com.acme.transport.network.hub.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional hub notice is in its life cycle. */
enum class RegionalHubNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A regional hub notice as the hub team keeps it. */
data class RegionalHubNotice(
    var code: String,
    var label: String,
    var score: Double,
    var channel: String,
    var unit: String,
    var pallets: Int,
    var visible: Boolean,
    var stopCount: Int,
    var windowDays: Int,
    var status: RegionalHubNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalHubNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalHubNoticeStatus): Boolean {
        when (status) {
            RegionalHubNoticeStatus.PROPOSED -> return next == RegionalHubNoticeStatus.WITHDRAWN
            RegionalHubNoticeStatus.APPROVED -> return next == RegionalHubNoticeStatus.REJECTED || next == RegionalHubNoticeStatus.WITHDRAWN
            RegionalHubNoticeStatus.REJECTED -> {
                when (next) {
                    RegionalHubNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalHubNoticeStatus) {
        if (!canMoveTo(next)) {
            error("regional hub notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedVolume(): Double = round(stopCount.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): RegionalHubNotice {
        return RegionalHubNotice(
            code = code,
            label = label,
            score = score,
            channel = channel,
            unit = unit,
            pallets = pallets,
            visible = visible,
            stopCount = stopCount,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional hub notice in status proposed. */
fun newRegionalHubNotice(code: String, label: String): RegionalHubNotice {
    return RegionalHubNotice(
        code = code,
        label = label,
        score = 0.0,
        channel = "",
        unit = "",
        pallets = 0,
        visible = false,
        stopCount = 0,
        windowDays = 0,
        status = RegionalHubNoticeStatus.PROPOSED,
    )
}
