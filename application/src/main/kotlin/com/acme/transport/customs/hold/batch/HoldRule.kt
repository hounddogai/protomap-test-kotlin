package com.acme.transport.customs.hold.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold rule is in its life cycle. */
enum class HoldRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A hold rule as the hold team keeps it. */
data class HoldRule(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var region: String,
    var visible: Boolean,
    var loadKg: Double,
    var status: HoldRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldRuleStatus): Boolean {
        when (status) {
            HoldRuleStatus.PROPOSED -> return next == HoldRuleStatus.APPROVED || next == HoldRuleStatus.REJECTED
            HoldRuleStatus.APPROVED -> {
                when (next) {
                    HoldRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
            HoldRuleStatus.REJECTED -> return next == HoldRuleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: HoldRuleStatus) {
        if (!canMoveTo(next)) {
            error("hold rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round(distanceKm * loadKg * 100) / 100

    fun snapshot(): HoldRule {
        return HoldRule(
            code = code,
            title = title,
            distanceKm = distanceKm,
            region = region,
            visible = visible,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a hold rule in status proposed. */
fun newHoldRule(code: String, title: String): HoldRule {
    return HoldRule(
        code = code,
        title = title,
        distanceKm = 0.0,
        region = "",
        visible = false,
        loadKg = 0.0,
        status = HoldRuleStatus.PROPOSED,
    )
}
