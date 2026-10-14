package com.acme.transport.freightpay.dispute.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted dispute rule records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a weighted dispute rule is in its life cycle. */
enum class WeightedDisputeRuleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted dispute rule. Its code is unique within a tenant. */
data class WeightedDisputeRule(
    var code: String,
    var label: String,
    var notes: String,
    var minimum: Double,
    var updatedAt: Int,
    var limit: Int,
    var revision: Int,
    var trailerId: String,
    var archived: Boolean,
    var status: WeightedDisputeRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDisputeRuleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDisputeRuleStatus): Boolean {
        when (status) {
            WeightedDisputeRuleStatus.OPEN -> return next == WeightedDisputeRuleStatus.RESOLVED
            WeightedDisputeRuleStatus.IN_REVIEW -> return next == WeightedDisputeRuleStatus.ARCHIVED
            WeightedDisputeRuleStatus.RESOLVED -> return next == WeightedDisputeRuleStatus.REOPENED || next == WeightedDisputeRuleStatus.ARCHIVED
            WeightedDisputeRuleStatus.REOPENED -> return next == WeightedDisputeRuleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: WeightedDisputeRuleStatus) {
        if (!canMoveTo(next)) {
            error("weighted dispute rule $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"weighted dispute rule"} $code"

    fun weightedExposure(): Double = round((revision.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedDisputeRule {
        return WeightedDisputeRule(
            code = code,
            label = label,
            notes = notes,
            minimum = minimum,
            updatedAt = updatedAt,
            limit = limit,
            revision = revision,
            trailerId = trailerId,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted dispute rule in status open. */
fun newWeightedDisputeRule(code: String, label: String): WeightedDisputeRule {
    return WeightedDisputeRule(
        code = code,
        label = label,
        notes = "",
        minimum = 0.0,
        updatedAt = 0,
        limit = 0,
        revision = 0,
        trailerId = "",
        archived = false,
        status = WeightedDisputeRuleStatus.OPEN,
    )
}
