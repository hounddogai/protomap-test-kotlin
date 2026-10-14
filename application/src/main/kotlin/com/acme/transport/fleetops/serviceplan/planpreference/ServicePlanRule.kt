package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan rule is in its life cycle. */
enum class ServicePlanRuleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A service plan rule as the service plan team keeps it. */
data class ServicePlanRule(
    var code: String,
    var label: String,
    var score: Double,
    var lane: String,
    var endsAt: Int,
    var status: ServicePlanRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanRuleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanRuleStatus): Boolean {
        when (status) {
            ServicePlanRuleStatus.OPEN -> {
                when (next) {
                    ServicePlanRuleStatus.REOPENED -> return true
                }
                return false
            }
            ServicePlanRuleStatus.IN_REVIEW -> return next == ServicePlanRuleStatus.REOPENED || next == ServicePlanRuleStatus.ARCHIVED
            ServicePlanRuleStatus.RESOLVED -> return next == ServicePlanRuleStatus.REOPENED || next == ServicePlanRuleStatus.ARCHIVED
            ServicePlanRuleStatus.REOPENED -> {
                when (next) {
                    ServicePlanRuleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanRuleStatus) {
        if (!canMoveTo(next)) {
            error("service plan rule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedTotal(): Double = round((endsAt.toDouble() + score) * 100) / 100

    fun snapshot(): ServicePlanRule {
        return ServicePlanRule(
            code = code,
            label = label,
            score = score,
            lane = lane,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a service plan rule in status open. */
fun newServicePlanRule(code: String, label: String): ServicePlanRule {
    return ServicePlanRule(
        code = code,
        label = label,
        score = 0.0,
        lane = "",
        endsAt = 0,
        status = ServicePlanRuleStatus.OPEN,
    )
}
