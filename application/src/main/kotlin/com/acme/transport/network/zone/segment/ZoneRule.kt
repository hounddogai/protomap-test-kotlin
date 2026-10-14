package com.acme.transport.network.zone.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone rule is in its life cycle. */
enum class ZoneRuleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One zone rule, with what the planners need to act on it. */
data class ZoneRule(
    var code: String,
    var title: String,
    var priority: Int,
    var attempts: Int,
    var etaMinutes: Int,
    var windowDays: Int,
    var weight: Double,
    var discount: Double,
    var country: String,
    var score: Double,
    var batchSize: Int,
    var status: ZoneRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneRuleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneRuleStatus): Boolean {
        when (status) {
            ZoneRuleStatus.OPEN -> return next == ZoneRuleStatus.RESOLVED || next == ZoneRuleStatus.REOPENED
            ZoneRuleStatus.IN_REVIEW -> return next == ZoneRuleStatus.REOPENED
            ZoneRuleStatus.RESOLVED -> {
                when (next) {
                    ZoneRuleStatus.REOPENED -> return true
                }
                return false
            }
            ZoneRuleStatus.REOPENED -> {
                when (next) {
                    ZoneRuleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneRuleStatus) {
        if (!canMoveTo(next)) {
            error("zone rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedValue(): Double = round(priority.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): ZoneRule {
        return ZoneRule(
            code = code,
            title = title,
            priority = priority,
            attempts = attempts,
            etaMinutes = etaMinutes,
            windowDays = windowDays,
            weight = weight,
            discount = discount,
            country = country,
            score = score,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a zone rule in status open. */
fun newZoneRule(code: String, title: String): ZoneRule {
    return ZoneRule(
        code = code,
        title = title,
        priority = 0,
        attempts = 0,
        etaMinutes = 0,
        windowDays = 0,
        weight = 0.0,
        discount = 0.0,
        country = "",
        score = 0.0,
        batchSize = 0,
        status = ZoneRuleStatus.OPEN,
    )
}
