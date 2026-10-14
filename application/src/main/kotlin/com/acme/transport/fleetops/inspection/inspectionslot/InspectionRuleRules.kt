package com.acme.transport.fleetops.inspection.inspectionslot

import com.acme.transport.kit.text.normalize

/** A rule a inspection rule must pass. Check returns its problems, or none. */
interface InspectionRuleRule {
    fun check(item: InspectionRule): MutableList<String>
}

data class InspectionRuleRequiredCodeRule(var message: String) : InspectionRuleRule {
    override fun check(item: InspectionRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionRuleAllowedExternalRefRule(var allowed: MutableList<String>) : InspectionRuleRule {
    override fun check(item: InspectionRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRuleRequiredExternalRefRule(var message: String) : InspectionRuleRule {
    override fun check(item: InspectionRule): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionRuleScoreRangeRule(var min: Double, var max: Double) : InspectionRuleRule {
    override fun check(item: InspectionRule): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class InspectionRuleRequiredTrailerIdRule(var message: String) : InspectionRuleRule {
    override fun check(item: InspectionRule): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection rule. */
data class InspectionRuleValidator(var rules: MutableList<InspectionRuleRule>) {
    fun validate(item: InspectionRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: InspectionRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionRule>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultInspectionRuleValidator(): InspectionRuleValidator {
    return InspectionRuleValidator(
        mutableListOf<InspectionRuleRule>(
            InspectionRuleRequiredCodeRule("inspection rule needs this field"),
            InspectionRuleAllowedExternalRefRule(mutableListOf<String>()),
            InspectionRuleRequiredExternalRefRule("inspection rule needs an entry"),
            InspectionRuleScoreRangeRule(9.1, 4401.2),
            InspectionRuleRequiredTrailerIdRule("inspection rule needs a value"),
        ),
    )
}
