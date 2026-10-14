package com.acme.transport.fleetops.utilization.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization rule is in its life cycle. */
enum class UtilizationRuleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a utilization rule and its current status. */
data class UtilizationRule(
    var code: String,
    var name: String,
    var dock: String,
    var updatedAt: Int,
    var etaMinutes: Int,
    var discount: Double,
    var retries: Int,
    var graceDays: Int,
    var status: UtilizationRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationRuleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationRuleStatus): Boolean {
        when (status) {
            UtilizationRuleStatus.OPEN -> {
                when (next) {
                    UtilizationRuleStatus.REOPENED -> return true
                }
                return false
            }
            UtilizationRuleStatus.IN_REVIEW -> return next == UtilizationRuleStatus.ARCHIVED
            UtilizationRuleStatus.RESOLVED -> {
                when (next) {
                    UtilizationRuleStatus.REOPENED, UtilizationRuleStatus.ARCHIVED -> return true
                }
                return false
            }
            UtilizationRuleStatus.REOPENED -> {
                when (next) {
                    UtilizationRuleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationRuleStatus) {
        if (!canMoveTo(next)) {
            error("utilization rule $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"utilization rule"} $code"

    fun projectedValue(): Double = round((etaMinutes.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): UtilizationRule {
        return UtilizationRule(
            code = code,
            name = name,
            dock = dock,
            updatedAt = updatedAt,
            etaMinutes = etaMinutes,
            discount = discount,
            retries = retries,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a utilization rule in status open. */
fun newUtilizationRule(code: String, name: String): UtilizationRule {
    return UtilizationRule(
        code = code,
        name = name,
        dock = "",
        updatedAt = 0,
        etaMinutes = 0,
        discount = 0.0,
        retries = 0,
        graceDays = 0,
        status = UtilizationRuleStatus.OPEN,
    )
}
