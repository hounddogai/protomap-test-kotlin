package com.acme.transport.safety.nearmiss.agreement

/** A rule a fallback near miss budget must pass. Check returns its problems, or none. */
interface FallbackNearMissBudgetRule {
    fun check(item: FallbackNearMissBudget): MutableList<String>
}

data class FallbackNearMissBudgetWindowDaysRangeRule(var min: Int, var max: Int) : FallbackNearMissBudgetRule {
    override fun check(item: FallbackNearMissBudget): MutableList<String> {
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

data class FallbackNearMissBudgetStopCountRangeRule(var min: Int, var max: Int) : FallbackNearMissBudgetRule {
    override fun check(item: FallbackNearMissBudget): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackNearMissBudgetTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : FallbackNearMissBudgetRule {
    override fun check(item: FallbackNearMissBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class FallbackNearMissBudgetRequiredLabelRule(var message: String) : FallbackNearMissBudgetRule {
    override fun check(item: FallbackNearMissBudget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackNearMissBudgetRouteCodeLengthRule(var maxLength: Int) : FallbackNearMissBudgetRule {
    override fun check(item: FallbackNearMissBudget): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback near miss budget. */
data class FallbackNearMissBudgetValidator(var rules: MutableList<FallbackNearMissBudgetRule>) {
    fun validate(item: FallbackNearMissBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackNearMissBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackNearMissBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackNearMissBudgetValidator(): FallbackNearMissBudgetValidator {
    return FallbackNearMissBudgetValidator(
        mutableListOf<FallbackNearMissBudgetRule>(
            FallbackNearMissBudgetWindowDaysRangeRule(120, 1000),
            FallbackNearMissBudgetStopCountRangeRule(1000, 86400),
            FallbackNearMissBudgetTagsLimitRule(1000, mutableListOf<String>()),
            FallbackNearMissBudgetRequiredLabelRule("fallback near miss budget needs an entry"),
            FallbackNearMissBudgetRouteCodeLengthRule(3600),
        ),
    )
}
