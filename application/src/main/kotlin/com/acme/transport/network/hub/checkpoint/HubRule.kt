package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub rule is in its life cycle. */
enum class HubRuleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One hub rule, with what the planners need to act on it. */
data class HubRule(
    var code: String,
    var title: String,
    var score: Double,
    var priority: Int,
    var label: String,
    var budget: Double,
    var weight: Double,
    var externalRef: String,
    var status: HubRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubRuleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubRuleStatus): Boolean {
        when (status) {
            HubRuleStatus.OPEN -> return next == HubRuleStatus.REOPENED || next == HubRuleStatus.ARCHIVED
            HubRuleStatus.IN_REVIEW -> {
                when (next) {
                    HubRuleStatus.REOPENED, HubRuleStatus.ARCHIVED -> return true
                }
                return false
            }
            HubRuleStatus.RESOLVED -> {
                when (next) {
                    HubRuleStatus.REOPENED -> return true
                }
                return false
            }
            HubRuleStatus.REOPENED -> {
                when (next) {
                    HubRuleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubRuleStatus) {
        if (!canMoveTo(next)) {
            error("hub rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun netLoad(): Double = round((weight - priority.toDouble()) * 100) / 100

    fun snapshot(): HubRule {
        return HubRule(
            code = code,
            title = title,
            score = score,
            priority = priority,
            label = label,
            budget = budget,
            weight = weight,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hub rule in status open. */
fun newHubRule(code: String, title: String): HubRule {
    return HubRule(
        code = code,
        title = title,
        score = 0.0,
        priority = 0,
        label = "",
        budget = 0.0,
        weight = 0.0,
        externalRef = "",
        status = HubRuleStatus.OPEN,
    )
}
