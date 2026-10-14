package com.acme.transport.crewing.swap.exception

/** A rule a global swap plan must pass. Check returns its problems, or none. */
interface GlobalSwapPlanRule {
    fun check(item: GlobalSwapPlan): MutableList<String>
}

data class GlobalSwapPlanAttemptsRangeRule(var min: Int, var max: Int) : GlobalSwapPlanRule {
    override fun check(item: GlobalSwapPlan): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class GlobalSwapPlanNameLengthRule(var maxLength: Int) : GlobalSwapPlanRule {
    override fun check(item: GlobalSwapPlan): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalSwapPlanRequiredNameRule(var message: String) : GlobalSwapPlanRule {
    override fun check(item: GlobalSwapPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalSwapPlanCodeLengthRule(var maxLength: Int) : GlobalSwapPlanRule {
    override fun check(item: GlobalSwapPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalSwapPlanRequiredSegmentRule(var message: String) : GlobalSwapPlanRule {
    override fun check(item: GlobalSwapPlan): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global swap plan. */
data class GlobalSwapPlanValidator(var rules: MutableList<GlobalSwapPlanRule>) {
    fun validate(item: GlobalSwapPlan): MutableList<String> {
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

    fun requireValid(item: GlobalSwapPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalSwapPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalSwapPlanValidator(): GlobalSwapPlanValidator {
    return GlobalSwapPlanValidator(
        mutableListOf<GlobalSwapPlanRule>(
            GlobalSwapPlanAttemptsRangeRule(32, 3600),
            GlobalSwapPlanNameLengthRule(1000),
            GlobalSwapPlanRequiredNameRule("global swap plan needs an entry"),
            GlobalSwapPlanCodeLengthRule(86400),
            GlobalSwapPlanRequiredSegmentRule("global swap plan needs this field"),
        ),
    )
}
