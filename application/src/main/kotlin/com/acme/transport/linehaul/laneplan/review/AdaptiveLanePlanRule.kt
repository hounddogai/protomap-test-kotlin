package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive lane plan rule is in its life cycle. */
enum class AdaptiveLanePlanRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One adaptive lane plan rule, with what the planners need to act on it. */
data class AdaptiveLanePlanRule(
    var code: String,
    var title: String,
    var weight: Double,
    var sequence: Int,
    var pallets: Int,
    var archived: Boolean,
    var category: String,
    var status: AdaptiveLanePlanRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLanePlanRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLanePlanRuleStatus): Boolean {
        when (status) {
            AdaptiveLanePlanRuleStatus.PROPOSED -> {
                when (next) {
                    AdaptiveLanePlanRuleStatus.REJECTED, AdaptiveLanePlanRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
            AdaptiveLanePlanRuleStatus.APPROVED -> return next == AdaptiveLanePlanRuleStatus.REJECTED || next == AdaptiveLanePlanRuleStatus.WITHDRAWN
            AdaptiveLanePlanRuleStatus.REJECTED -> {
                when (next) {
                    AdaptiveLanePlanRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveLanePlanRuleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive lane plan rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedLoad(): Double = round((sequence.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveLanePlanRule {
        return AdaptiveLanePlanRule(
            code = code,
            title = title,
            weight = weight,
            sequence = sequence,
            pallets = pallets,
            archived = archived,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a adaptive lane plan rule in status proposed. */
fun newAdaptiveLanePlanRule(code: String, title: String): AdaptiveLanePlanRule {
    return AdaptiveLanePlanRule(
        code = code,
        title = title,
        weight = 0.0,
        sequence = 0,
        pallets = 0,
        archived = false,
        category = "",
        status = AdaptiveLanePlanRuleStatus.PROPOSED,
    )
}
