package com.acme.transport.network.hub.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub tier is in its life cycle. */
enum class HubTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One hub tier, with what the planners need to act on it. */
data class HubTier(
    var code: String,
    var title: String,
    var limit: Int,
    var odometer: Int,
    var summary: String,
    var endsAt: Int,
    var visible: Boolean,
    var lane: String,
    var status: HubTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: HubTierStatus): Boolean {
        when (status) {
            HubTierStatus.PROPOSED -> {
                when (next) {
                    HubTierStatus.APPROVED -> return true
                }
                return false
            }
            HubTierStatus.APPROVED -> return next == HubTierStatus.REJECTED || next == HubTierStatus.WITHDRAWN
            HubTierStatus.REJECTED -> {
                when (next) {
                    HubTierStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubTierStatus) {
        if (!canMoveTo(next)) {
            error("hub tier $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveExposure(): Double = round(limit.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): HubTier {
        return HubTier(
            code = code,
            title = title,
            limit = limit,
            odometer = odometer,
            summary = summary,
            endsAt = endsAt,
            visible = visible,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a hub tier in status proposed. */
fun newHubTier(code: String, title: String): HubTier {
    return HubTier(
        code = code,
        title = title,
        limit = 0,
        odometer = 0,
        summary = "",
        endsAt = 0,
        visible = false,
        lane = "",
        status = HubTierStatus.PROPOSED,
    )
}
