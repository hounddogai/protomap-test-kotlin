package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a rolling swap plan must pass. Check returns its problems, or none. */
interface RollingSwapPlanRule {
    fun check(item: RollingSwapPlan): MutableList<String>
}

data class RollingSwapPlanCodePrefixRule(var prefix: String) : RollingSwapPlanRule {
    override fun check(item: RollingSwapPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapPlanGraceDaysRangeRule(var min: Int, var max: Int) : RollingSwapPlanRule {
    override fun check(item: RollingSwapPlan): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class RollingSwapPlanRequiredNameRule(var message: String) : RollingSwapPlanRule {
    override fun check(item: RollingSwapPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapPlanCodeLengthRule(var maxLength: Int) : RollingSwapPlanRule {
    override fun check(item: RollingSwapPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapPlanAllowedCodeRule(var allowed: MutableList<String>) : RollingSwapPlanRule {
    override fun check(item: RollingSwapPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapPlanRequiredCodeRule(var message: String) : RollingSwapPlanRule {
    override fun check(item: RollingSwapPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling swap plan. */
data class RollingSwapPlanValidator(var rules: MutableList<RollingSwapPlanRule>) {
    fun validate(item: RollingSwapPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingSwapPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingSwapPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingSwapPlanValidator(): RollingSwapPlanValidator {
    return RollingSwapPlanValidator(
        mutableListOf<RollingSwapPlanRule>(
            RollingSwapPlanCodePrefixRule("OPS"),
            RollingSwapPlanGraceDaysRangeRule(64, 1000),
            RollingSwapPlanRequiredNameRule("rolling swap plan needs an entry"),
            RollingSwapPlanCodeLengthRule(3600),
            RollingSwapPlanAllowedCodeRule(mutableListOf<String>()),
            RollingSwapPlanRequiredCodeRule("rolling swap plan needs a value"),
        ),
    )
}
