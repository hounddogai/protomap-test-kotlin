package com.acme.transport.linehaul.trailer.budget

/** A rule a trailer budget must pass. Check returns its problems, or none. */
interface TrailerBudgetRule {
    fun check(item: TrailerBudget): MutableList<String>
}

data class TrailerBudgetRequiredCodeRule(var message: String) : TrailerBudgetRule {
    override fun check(item: TrailerBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerBudgetWindowDaysRangeRule(var min: Int, var max: Int) : TrailerBudgetRule {
    override fun check(item: TrailerBudget): MutableList<String> {
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

data class TrailerBudgetRevisionRangeRule(var min: Int, var max: Int) : TrailerBudgetRule {
    override fun check(item: TrailerBudget): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerBudgetCodePrefixRule(var prefix: String) : TrailerBudgetRule {
    override fun check(item: TrailerBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerBudgetChecksumLengthRule(var maxLength: Int) : TrailerBudgetRule {
    override fun check(item: TrailerBudget): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer budget. */
data class TrailerBudgetValidator(var rules: MutableList<TrailerBudgetRule>) {
    fun validate(item: TrailerBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerBudgetValidator(): TrailerBudgetValidator {
    return TrailerBudgetValidator(
        mutableListOf<TrailerBudgetRule>(
            TrailerBudgetRequiredCodeRule("trailer budget needs this field"),
            TrailerBudgetWindowDaysRangeRule(3600, 86400),
            TrailerBudgetRevisionRangeRule(16, 120),
            TrailerBudgetCodePrefixRule("ACM"),
            TrailerBudgetChecksumLengthRule(255),
        ),
    )
}
