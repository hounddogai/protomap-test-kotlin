package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul rule is in its life cycle. */
enum class BackhaulRuleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A backhaul rule. Its code is unique within a tenant. */
data class BackhaulRule(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var windowDays: Int,
    var cost: Double,
    var batchSize: Int,
    var status: BackhaulRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulRuleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulRuleStatus): Boolean {
        when (status) {
            BackhaulRuleStatus.PLANNED -> return next == BackhaulRuleStatus.CANCELLED
            BackhaulRuleStatus.SCHEDULED -> {
                when (next) {
                    BackhaulRuleStatus.IN_PROGRESS, BackhaulRuleStatus.COMPLETED -> return true
                }
                return false
            }
            BackhaulRuleStatus.IN_PROGRESS -> {
                when (next) {
                    BackhaulRuleStatus.COMPLETED -> return true
                }
                return false
            }
            BackhaulRuleStatus.COMPLETED -> {
                when (next) {
                    BackhaulRuleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulRuleStatus) {
        if (!canMoveTo(next)) {
            error("backhaul rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"backhaul rule"} $code"

    fun effectiveExposure(): Double = round((cost + windowDays.toDouble()) * 100) / 100

    fun snapshot(): BackhaulRule {
        return BackhaulRule(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            windowDays = windowDays,
            cost = cost,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a backhaul rule in status planned. */
fun newBackhaulRule(code: String, name: String): BackhaulRule {
    return BackhaulRule(
        code = code,
        name = name,
        fuelLiters = 0.0,
        windowDays = 0,
        cost = 0.0,
        batchSize = 0,
        status = BackhaulRuleStatus.PLANNED,
    )
}
