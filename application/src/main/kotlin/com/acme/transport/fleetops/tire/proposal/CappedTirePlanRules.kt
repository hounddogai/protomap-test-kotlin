package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.text.normalize

/** A rule a capped tire plan must pass. Check returns its problems, or none. */
interface CappedTirePlanRule {
    fun check(item: CappedTirePlan): MutableList<String>
}

data class CappedTirePlanEndsAtRangeRule(var min: Int, var max: Int) : CappedTirePlanRule {
    override fun check(item: CappedTirePlan): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class CappedTirePlanAllowedCodeRule(var allowed: MutableList<String>) : CappedTirePlanRule {
    override fun check(item: CappedTirePlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class CappedTirePlanRequiredCodeRule(var message: String) : CappedTirePlanRule {
    override fun check(item: CappedTirePlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped tire plan. */
data class CappedTirePlanValidator(var rules: MutableList<CappedTirePlanRule>) {
    fun validate(item: CappedTirePlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedTirePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTirePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTirePlanValidator(): CappedTirePlanValidator {
    return CappedTirePlanValidator(
        mutableListOf<CappedTirePlanRule>(
            CappedTirePlanEndsAtRangeRule(80, 255),
            CappedTirePlanAllowedCodeRule(mutableListOf<String>()),
            CappedTirePlanRequiredCodeRule("capped tire plan needs this field"),
        ),
    )
}
