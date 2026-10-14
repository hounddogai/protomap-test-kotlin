package com.acme.transport.safety.incident.digest

import com.acme.transport.kit.text.normalize

/** A rule a incident rule must pass. Check returns its problems, or none. */
interface IncidentRuleRule {
    fun check(item: IncidentRule): MutableList<String>
}

data class IncidentRuleRouteCodeLengthRule(var maxLength: Int) : IncidentRuleRule {
    override fun check(item: IncidentRule): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentRuleRequiredRouteCodeRule(var message: String) : IncidentRuleRule {
    override fun check(item: IncidentRule): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentRuleAllowedCodeRule(var allowed: MutableList<String>) : IncidentRuleRule {
    override fun check(item: IncidentRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident rule. */
data class IncidentRuleValidator(var rules: MutableList<IncidentRuleRule>) {
    fun validate(item: IncidentRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentRule>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentRuleValidator(): IncidentRuleValidator {
    return IncidentRuleValidator(
        mutableListOf<IncidentRuleRule>(
            IncidentRuleRouteCodeLengthRule(32),
            IncidentRuleRequiredRouteCodeRule("incident rule needs this field"),
            IncidentRuleAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
