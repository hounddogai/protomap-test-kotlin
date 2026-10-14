package com.acme.transport.lastmile.drop.allocation

/** A rule a drop plan must pass. Check returns its problems, or none. */
interface DropPlanRule {
    fun check(item: DropPlan): MutableList<String>
}

data class DropPlanRegionLengthRule(var maxLength: Int) : DropPlanRule {
    override fun check(item: DropPlan): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropPlanEndsAtRangeRule(var min: Int, var max: Int) : DropPlanRule {
    override fun check(item: DropPlan): MutableList<String> {
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

data class DropPlanRequiredCodeRule(var message: String) : DropPlanRule {
    override fun check(item: DropPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropPlanCodePrefixRule(var prefix: String) : DropPlanRule {
    override fun check(item: DropPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DropPlanWindowDaysRangeRule(var min: Int, var max: Int) : DropPlanRule {
    override fun check(item: DropPlan): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class DropPlanRequiredTitleRule(var message: String) : DropPlanRule {
    override fun check(item: DropPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop plan. */
data class DropPlanValidator(var rules: MutableList<DropPlanRule>) {
    fun validate(item: DropPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultDropPlanValidator(): DropPlanValidator {
    return DropPlanValidator(
        mutableListOf<DropPlanRule>(
            DropPlanRegionLengthRule(3600),
            DropPlanEndsAtRangeRule(3600, 86400),
            DropPlanRequiredCodeRule("drop plan needs this field"),
            DropPlanCodePrefixRule("Q"),
            DropPlanWindowDaysRangeRule(80, 1000),
            DropPlanRequiredTitleRule("drop plan needs an entry"),
        ),
    )
}
