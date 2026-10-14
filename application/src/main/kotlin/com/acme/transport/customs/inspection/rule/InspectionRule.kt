package com.acme.transport.customs.inspection.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection rule records of customs's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection rule is in its life cycle. */
enum class InspectionRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One inspection rule, with what the planners need to act on it. */
data class InspectionRule(
    var code: String,
    var label: String,
    var contactEmail: String,
    var attributes: MutableMap<String, String>,
    var floorValue: Double,
    var shiftCode: String,
    var region: String,
    var budget: Double,
    var status: InspectionRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionRuleStatus): Boolean {
        when (status) {
            InspectionRuleStatus.REQUESTED -> return next == InspectionRuleStatus.GRANTED || next == InspectionRuleStatus.EXPIRED
            InspectionRuleStatus.GRANTED -> return next == InspectionRuleStatus.REVOKED || next == InspectionRuleStatus.EXPIRED
            InspectionRuleStatus.REVOKED -> {
                when (next) {
                    InspectionRuleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionRuleStatus) {
        if (!canMoveTo(next)) {
            error("inspection rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"inspection rule"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((floorValue + budget) * 100) / 100

    fun snapshot(): InspectionRule {
        return InspectionRule(
            code = code,
            label = label,
            contactEmail = contactEmail,
            attributes = attributes,
            floorValue = floorValue,
            shiftCode = shiftCode,
            region = region,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a inspection rule in status requested. */
fun newInspectionRule(code: String, label: String): InspectionRule {
    return InspectionRule(
        code = code,
        label = label,
        contactEmail = "",
        attributes = mutableMapOf<String, String>(),
        floorValue = 0.0,
        shiftCode = "",
        region = "",
        budget = 0.0,
        status = InspectionRuleStatus.REQUESTED,
    )
}
