package com.acme.transport.crewing.qualification.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling qualification rule records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a rolling qualification rule is in its life cycle. */
enum class RollingQualificationRuleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling qualification rule as the qualification team keeps it. */
data class RollingQualificationRule(
    var code: String,
    var title: String,
    var startsAt: Int,
    var country: String,
    var distanceKm: Double,
    var depot: String,
    var amount: Double,
    var floorValue: Double,
    var stopCount: Int,
    var region: String,
    var status: RollingQualificationRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingQualificationRuleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingQualificationRuleStatus): Boolean {
        when (status) {
            RollingQualificationRuleStatus.NEW -> {
                when (next) {
                    RollingQualificationRuleStatus.RUNNING, RollingQualificationRuleStatus.DONE -> return true
                }
                return false
            }
            RollingQualificationRuleStatus.QUEUED -> {
                when (next) {
                    RollingQualificationRuleStatus.FAILED -> return true
                }
                return false
            }
            RollingQualificationRuleStatus.RUNNING -> return next == RollingQualificationRuleStatus.FAILED
            RollingQualificationRuleStatus.DONE -> {
                when (next) {
                    RollingQualificationRuleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingQualificationRuleStatus) {
        if (!canMoveTo(next)) {
            error("rolling qualification rule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveValue(): Double = round((stopCount.toDouble() + amount) * 100) / 100

    fun snapshot(): RollingQualificationRule {
        return RollingQualificationRule(
            code = code,
            title = title,
            startsAt = startsAt,
            country = country,
            distanceKm = distanceKm,
            depot = depot,
            amount = amount,
            floorValue = floorValue,
            stopCount = stopCount,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rolling qualification rule in status new. */
fun newRollingQualificationRule(code: String, title: String): RollingQualificationRule {
    return RollingQualificationRule(
        code = code,
        title = title,
        startsAt = 0,
        country = "",
        distanceKm = 0.0,
        depot = "",
        amount = 0.0,
        floorValue = 0.0,
        stopCount = 0,
        region = "",
        status = RollingQualificationRuleStatus.NEW,
    )
}
