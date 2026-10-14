package com.acme.transport.network.transittime.overrideitem

/** A rule a primary transit time budget must pass. Check returns its problems, or none. */
interface PrimaryTransitTimeBudgetRule {
    fun check(item: PrimaryTransitTimeBudget): MutableList<String>
}

data class PrimaryTransitTimeBudgetRequiredNameRule(var message: String) : PrimaryTransitTimeBudgetRule {
    override fun check(item: PrimaryTransitTimeBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeBudgetCodeLengthRule(var maxLength: Int) : PrimaryTransitTimeBudgetRule {
    override fun check(item: PrimaryTransitTimeBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeBudgetCodePrefixRule(var prefix: String) : PrimaryTransitTimeBudgetRule {
    override fun check(item: PrimaryTransitTimeBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeBudgetUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryTransitTimeBudgetRule {
    override fun check(item: PrimaryTransitTimeBudget): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary transit time budget. */
data class PrimaryTransitTimeBudgetValidator(var rules: MutableList<PrimaryTransitTimeBudgetRule>) {
    fun validate(item: PrimaryTransitTimeBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryTransitTimeBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTransitTimeBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTransitTimeBudgetValidator(): PrimaryTransitTimeBudgetValidator {
    return PrimaryTransitTimeBudgetValidator(
        mutableListOf<PrimaryTransitTimeBudgetRule>(
            PrimaryTransitTimeBudgetRequiredNameRule("primary transit time budget needs this field"),
            PrimaryTransitTimeBudgetCodeLengthRule(1000),
            PrimaryTransitTimeBudgetCodePrefixRule("REF"),
            PrimaryTransitTimeBudgetUpdatedAtRangeRule(255, 1000),
        ),
    )
}
