package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient relay rule records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a lenient relay rule is in its life cycle. */
enum class LenientRelayRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a lenient relay rule and its current status. */
data class LenientRelayRule(
    var code: String,
    var name: String,
    var odometer: Int,
    var margin: Double,
    var ceiling: Double,
    var timezone: String,
    var country: String,
    var durationMinutes: Int,
    var unit: String,
    var status: LenientRelayRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRelayRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRelayRuleStatus): Boolean {
        when (status) {
            LenientRelayRuleStatus.DRAFT -> return next == LenientRelayRuleStatus.PENDING || next == LenientRelayRuleStatus.CLOSED
            LenientRelayRuleStatus.PENDING -> {
                when (next) {
                    LenientRelayRuleStatus.ACTIVE, LenientRelayRuleStatus.CLOSED -> return true
                }
                return false
            }
            LenientRelayRuleStatus.ACTIVE -> return next == LenientRelayRuleStatus.SUSPENDED || next == LenientRelayRuleStatus.CLOSED
            LenientRelayRuleStatus.SUSPENDED -> {
                when (next) {
                    LenientRelayRuleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientRelayRuleStatus) {
        if (!canMoveTo(next)) {
            error("lenient relay rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round((odometer.toDouble() + margin) * 100) / 100

    fun snapshot(): LenientRelayRule {
        return LenientRelayRule(
            code = code,
            name = name,
            odometer = odometer,
            margin = margin,
            ceiling = ceiling,
            timezone = timezone,
            country = country,
            durationMinutes = durationMinutes,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient relay rule in status draft. */
fun newLenientRelayRule(code: String, name: String): LenientRelayRule {
    return LenientRelayRule(
        code = code,
        name = name,
        odometer = 0,
        margin = 0.0,
        ceiling = 0.0,
        timezone = "",
        country = "",
        durationMinutes = 0,
        unit = "",
        status = LenientRelayRuleStatus.DRAFT,
    )
}
