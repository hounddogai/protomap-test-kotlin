package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card milestone is in its life cycle. */
enum class RateCardMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rate card milestone as the rate card team keeps it. */
data class RateCardMilestone(
    var code: String,
    var title: String,
    var reviewer: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var routeCode: String,
    var ceiling: Double,
    var floorValue: Double,
    var region: String,
    var startsAt: Int,
    var etaMinutes: Int,
    var status: RateCardMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardMilestoneStatus): Boolean {
        when (status) {
            RateCardMilestoneStatus.DRAFT -> {
                when (next) {
                    RateCardMilestoneStatus.PENDING -> return true
                }
                return false
            }
            RateCardMilestoneStatus.PENDING -> return next == RateCardMilestoneStatus.ACTIVE
            RateCardMilestoneStatus.ACTIVE -> {
                when (next) {
                    RateCardMilestoneStatus.CLOSED -> return true
                }
                return false
            }
            RateCardMilestoneStatus.SUSPENDED -> {
                when (next) {
                    RateCardMilestoneStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("rate card milestone $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rate card milestone"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round(startsAt.toDouble() * ceiling * 100) / 100

    fun snapshot(): RateCardMilestone {
        return RateCardMilestone(
            code = code,
            title = title,
            reviewer = reviewer,
            name = name,
            attributes = attributes,
            routeCode = routeCode,
            ceiling = ceiling,
            floorValue = floorValue,
            region = region,
            startsAt = startsAt,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rate card milestone in status draft. */
fun newRateCardMilestone(code: String, title: String): RateCardMilestone {
    return RateCardMilestone(
        code = code,
        title = title,
        reviewer = "",
        name = "",
        attributes = mutableMapOf<String, String>(),
        routeCode = "",
        ceiling = 0.0,
        floorValue = 0.0,
        region = "",
        startsAt = 0,
        etaMinutes = 0,
        status = RateCardMilestoneStatus.DRAFT,
    )
}
