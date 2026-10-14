package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled hub rule is in its life cycle. */
enum class ScheduledHubRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a scheduled hub rule and its current status. */
data class ScheduledHubRule(
    var code: String,
    var name: String,
    var active: Boolean,
    var loadKg: Double,
    var vehicleId: String,
    var amount: Double,
    var status: ScheduledHubRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHubRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHubRuleStatus): Boolean {
        when (status) {
            ScheduledHubRuleStatus.IDLE -> {
                when (next) {
                    ScheduledHubRuleStatus.READY -> return true
                }
                return false
            }
            ScheduledHubRuleStatus.WARMING -> {
                when (next) {
                    ScheduledHubRuleStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledHubRuleStatus.READY -> return next == ScheduledHubRuleStatus.DRAINING || next == ScheduledHubRuleStatus.STOPPED
            ScheduledHubRuleStatus.DRAINING -> return next == ScheduledHubRuleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledHubRuleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hub rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedVolume(): Double = round((loadKg - amount) * 100) / 100

    fun snapshot(): ScheduledHubRule {
        return ScheduledHubRule(
            code = code,
            name = name,
            active = active,
            loadKg = loadKg,
            vehicleId = vehicleId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled hub rule in status idle. */
fun newScheduledHubRule(code: String, name: String): ScheduledHubRule {
    return ScheduledHubRule(
        code = code,
        name = name,
        active = false,
        loadKg = 0.0,
        vehicleId = "",
        amount = 0.0,
        status = ScheduledHubRuleStatus.IDLE,
    )
}
