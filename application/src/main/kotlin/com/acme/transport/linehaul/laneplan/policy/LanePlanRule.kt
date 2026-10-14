package com.acme.transport.linehaul.laneplan.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan rule is in its life cycle. */
enum class LanePlanRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One lane plan rule, with what the planners need to act on it. */
data class LanePlanRule(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var lane: String,
    var carrier: String,
    var minimum: Double,
    var notes: String,
    var status: LanePlanRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanRuleStatus): Boolean {
        when (status) {
            LanePlanRuleStatus.CREATED -> {
                when (next) {
                    LanePlanRuleStatus.VALIDATED, LanePlanRuleStatus.RETIRED -> return true
                }
                return false
            }
            LanePlanRuleStatus.VALIDATED -> {
                when (next) {
                    LanePlanRuleStatus.PUBLISHED, LanePlanRuleStatus.RETIRED -> return true
                }
                return false
            }
            LanePlanRuleStatus.PUBLISHED -> return next == LanePlanRuleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LanePlanRuleStatus) {
        if (!canMoveTo(next)) {
            error("lane plan rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedValue(): Double = round((minimum - fuelLiters) * 100) / 100

    fun snapshot(): LanePlanRule {
        return LanePlanRule(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            lane = lane,
            carrier = carrier,
            minimum = minimum,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lane plan rule in status created. */
fun newLanePlanRule(code: String, name: String): LanePlanRule {
    return LanePlanRule(
        code = code,
        name = name,
        fuelLiters = 0.0,
        lane = "",
        carrier = "",
        minimum = 0.0,
        notes = "",
        status = LanePlanRuleStatus.CREATED,
    )
}
