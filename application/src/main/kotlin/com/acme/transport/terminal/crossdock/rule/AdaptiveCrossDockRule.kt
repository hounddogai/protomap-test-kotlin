package com.acme.transport.terminal.crossdock.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive cross dock rule records of terminal's cross dock work, and the rules, stores, and calculations
// around them.

/** Where a adaptive cross dock rule is in its life cycle. */
enum class AdaptiveCrossDockRuleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One adaptive cross dock rule, with what the planners need to act on it. */
data class AdaptiveCrossDockRule(
    var code: String,
    var name: String,
    var sequence: Int,
    var amount: Double,
    var createdAt: Int,
    var archived: Boolean,
    var graceDays: Int,
    var label: String,
    var status: AdaptiveCrossDockRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCrossDockRuleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCrossDockRuleStatus): Boolean {
        when (status) {
            AdaptiveCrossDockRuleStatus.OPEN -> {
                when (next) {
                    AdaptiveCrossDockRuleStatus.IN_REVIEW, AdaptiveCrossDockRuleStatus.REOPENED -> return true
                }
                return false
            }
            AdaptiveCrossDockRuleStatus.IN_REVIEW -> return next == AdaptiveCrossDockRuleStatus.RESOLVED
            AdaptiveCrossDockRuleStatus.RESOLVED -> {
                when (next) {
                    AdaptiveCrossDockRuleStatus.REOPENED, AdaptiveCrossDockRuleStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveCrossDockRuleStatus.REOPENED -> return next == AdaptiveCrossDockRuleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AdaptiveCrossDockRuleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive cross dock rule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"adaptive cross dock rule"} $code"

    fun weightedVolume(): Double = round((createdAt.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveCrossDockRule {
        return AdaptiveCrossDockRule(
            code = code,
            name = name,
            sequence = sequence,
            amount = amount,
            createdAt = createdAt,
            archived = archived,
            graceDays = graceDays,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive cross dock rule in status open. */
fun newAdaptiveCrossDockRule(code: String, name: String): AdaptiveCrossDockRule {
    return AdaptiveCrossDockRule(
        code = code,
        name = name,
        sequence = 0,
        amount = 0.0,
        createdAt = 0,
        archived = false,
        graceDays = 0,
        label = "",
        status = AdaptiveCrossDockRuleStatus.OPEN,
    )
}
