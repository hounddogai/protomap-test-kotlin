package com.acme.transport.network.hub.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub budget is in its life cycle. */
enum class HubBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A hub budget as the hub team keeps it. */
data class HubBudget(
    var code: String,
    var title: String,
    var minimum: Double,
    var pinned: Boolean,
    var limit: Int,
    var createdAt: Int,
    var timezone: String,
    var rate: Double,
    var status: HubBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubBudgetStatus): Boolean {
        when (status) {
            HubBudgetStatus.IDLE -> return next == HubBudgetStatus.READY || next == HubBudgetStatus.STOPPED
            HubBudgetStatus.WARMING -> {
                when (next) {
                    HubBudgetStatus.DRAINING -> return true
                }
                return false
            }
            HubBudgetStatus.READY -> {
                when (next) {
                    HubBudgetStatus.DRAINING -> return true
                }
                return false
            }
            HubBudgetStatus.DRAINING -> return next == HubBudgetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: HubBudgetStatus) {
        if (!canMoveTo(next)) {
            error("hub budget $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netExposure(): Double = round((minimum + createdAt.toDouble()) * 100) / 100

    fun snapshot(): HubBudget {
        return HubBudget(
            code = code,
            title = title,
            minimum = minimum,
            pinned = pinned,
            limit = limit,
            createdAt = createdAt,
            timezone = timezone,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hub budget in status idle. */
fun newHubBudget(code: String, title: String): HubBudget {
    return HubBudget(
        code = code,
        title = title,
        minimum = 0.0,
        pinned = false,
        limit = 0,
        createdAt = 0,
        timezone = "",
        rate = 0.0,
        status = HubBudgetStatus.IDLE,
    )
}
