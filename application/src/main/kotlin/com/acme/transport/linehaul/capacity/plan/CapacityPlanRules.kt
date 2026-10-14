package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.text.normalize

/** A rule a capacity plan must pass. Check returns its problems, or none. */
interface CapacityPlanRule {
    fun check(item: CapacityPlan): MutableList<String>
}

data class CapacityPlanRequiredOwnerIdRule(var message: String) : CapacityPlanRule {
    override fun check(item: CapacityPlan): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityPlanCeilingRangeRule(var min: Double, var max: Double) : CapacityPlanRule {
    override fun check(item: CapacityPlan): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class CapacityPlanAllowedCodeRule(var allowed: MutableList<String>) : CapacityPlanRule {
    override fun check(item: CapacityPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class CapacityPlanCodePrefixRule(var prefix: String) : CapacityPlanRule {
    override fun check(item: CapacityPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity plan. */
data class CapacityPlanValidator(var rules: MutableList<CapacityPlanRule>) {
    fun validate(item: CapacityPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityPlanValidator(): CapacityPlanValidator {
    return CapacityPlanValidator(
        mutableListOf<CapacityPlanRule>(
            CapacityPlanRequiredOwnerIdRule("capacity plan needs this field"),
            CapacityPlanCeilingRangeRule(9.2, 1713.4),
            CapacityPlanAllowedCodeRule(mutableListOf<String>()),
            CapacityPlanCodePrefixRule("X"),
        ),
    )
}
