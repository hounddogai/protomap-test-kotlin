package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient lane plan rule records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a lenient lane plan rule is in its life cycle. */
enum class LenientLanePlanRuleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lenient lane plan rule as the lane plan team keeps it. */
data class LenientLanePlanRule(
    var code: String,
    var title: String,
    var budget: Double,
    var archived: Boolean,
    var revision: Int,
    var fuelLiters: Double,
    var batchSize: Int,
    var capacity: Int,
    var amount: Double,
    var status: LenientLanePlanRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientLanePlanRuleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientLanePlanRuleStatus): Boolean {
        when (status) {
            LenientLanePlanRuleStatus.OPEN -> {
                when (next) {
                    LenientLanePlanRuleStatus.RESOLVED, LenientLanePlanRuleStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientLanePlanRuleStatus.IN_REVIEW -> {
                when (next) {
                    LenientLanePlanRuleStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientLanePlanRuleStatus.RESOLVED -> {
                when (next) {
                    LenientLanePlanRuleStatus.REOPENED, LenientLanePlanRuleStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientLanePlanRuleStatus.REOPENED -> return next == LenientLanePlanRuleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LenientLanePlanRuleStatus) {
        if (!canMoveTo(next)) {
            error("lenient lane plan rule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"lenient lane plan rule"} $code"

    fun weightedTotal(): Double = round(batchSize.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): LenientLanePlanRule {
        return LenientLanePlanRule(
            code = code,
            title = title,
            budget = budget,
            archived = archived,
            revision = revision,
            fuelLiters = fuelLiters,
            batchSize = batchSize,
            capacity = capacity,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient lane plan rule in status open. */
fun newLenientLanePlanRule(code: String, title: String): LenientLanePlanRule {
    return LenientLanePlanRule(
        code = code,
        title = title,
        budget = 0.0,
        archived = false,
        revision = 0,
        fuelLiters = 0.0,
        batchSize = 0,
        capacity = 0,
        amount = 0.0,
        status = LenientLanePlanRuleStatus.OPEN,
    )
}
