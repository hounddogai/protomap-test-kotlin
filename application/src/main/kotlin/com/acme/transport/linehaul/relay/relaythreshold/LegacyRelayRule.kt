package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy relay rule is in its life cycle. */
enum class LegacyRelayRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One legacy relay rule, with what the planners need to act on it. */
data class LegacyRelayRule(
    var code: String,
    var title: String,
    var lane: String,
    var version: Int,
    var updatedAt: Int,
    var revision: Int,
    var priority: Int,
    var status: LegacyRelayRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRelayRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRelayRuleStatus): Boolean {
        when (status) {
            LegacyRelayRuleStatus.IDLE -> {
                when (next) {
                    LegacyRelayRuleStatus.READY, LegacyRelayRuleStatus.STOPPED -> return true
                }
                return false
            }
            LegacyRelayRuleStatus.WARMING -> return next == LegacyRelayRuleStatus.READY
            LegacyRelayRuleStatus.READY -> return next == LegacyRelayRuleStatus.DRAINING
            LegacyRelayRuleStatus.DRAINING -> {
                when (next) {
                    LegacyRelayRuleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyRelayRuleStatus) {
        if (!canMoveTo(next)) {
            error("legacy relay rule $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code / $title"

    fun weightedExposure(): Double = round((version.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): LegacyRelayRule {
        return LegacyRelayRule(
            code = code,
            title = title,
            lane = lane,
            version = version,
            updatedAt = updatedAt,
            revision = revision,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a legacy relay rule in status idle. */
fun newLegacyRelayRule(code: String, title: String): LegacyRelayRule {
    return LegacyRelayRule(
        code = code,
        title = title,
        lane = "",
        version = 0,
        updatedAt = 0,
        revision = 0,
        priority = 0,
        status = LegacyRelayRuleStatus.IDLE,
    )
}
