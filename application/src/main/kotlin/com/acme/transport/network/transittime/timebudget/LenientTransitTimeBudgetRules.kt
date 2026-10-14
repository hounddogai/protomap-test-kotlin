package com.acme.transport.network.transittime.timebudget

/** A rule a lenient transit time budget must pass. Check returns its problems, or none. */
interface LenientTransitTimeBudgetRule {
    fun check(item: LenientTransitTimeBudget): MutableList<String>
}

data class LenientTransitTimeBudgetCodeLengthRule(var maxLength: Int) : LenientTransitTimeBudgetRule {
    override fun check(item: LenientTransitTimeBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientTransitTimeBudgetThresholdRangeRule(
    var min: Double,
    var max: Double,
) : LenientTransitTimeBudgetRule {
    override fun check(item: LenientTransitTimeBudget): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class LenientTransitTimeBudgetRequiredNameRule(var message: String) : LenientTransitTimeBudgetRule {
    override fun check(item: LenientTransitTimeBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientTransitTimeBudgetTitleLengthRule(var maxLength: Int) : LenientTransitTimeBudgetRule {
    override fun check(item: LenientTransitTimeBudget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientTransitTimeBudgetSequenceRangeRule(
    var min: Int,
    var max: Int,
) : LenientTransitTimeBudgetRule {
    override fun check(item: LenientTransitTimeBudget): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientTransitTimeBudgetRequiredTitleRule(var message: String) : LenientTransitTimeBudgetRule {
    override fun check(item: LenientTransitTimeBudget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient transit time budget. */
data class LenientTransitTimeBudgetValidator(var rules: MutableList<LenientTransitTimeBudgetRule>) {
    fun validate(item: LenientTransitTimeBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientTransitTimeBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientTransitTimeBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientTransitTimeBudgetValidator(): LenientTransitTimeBudgetValidator {
    return LenientTransitTimeBudgetValidator(
        mutableListOf<LenientTransitTimeBudgetRule>(
            LenientTransitTimeBudgetCodeLengthRule(86400),
            LenientTransitTimeBudgetThresholdRangeRule(6.2, 1443.8),
            LenientTransitTimeBudgetRequiredNameRule("lenient transit time budget needs a value"),
            LenientTransitTimeBudgetTitleLengthRule(32),
            LenientTransitTimeBudgetSequenceRangeRule(80, 3600),
            LenientTransitTimeBudgetRequiredTitleRule("lenient transit time budget needs this field"),
        ),
    )
}
