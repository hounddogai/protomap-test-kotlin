package com.acme.transport.freightpay.ratecard.cycle

/** A rule a rolling rate card summary must pass. Check returns its problems, or none. */
interface RollingRateCardSummaryRule {
    fun check(item: RollingRateCardSummary): MutableList<String>
}

data class RollingRateCardSummaryBudgetRangeRule(
    var min: Double,
    var max: Double,
) : RollingRateCardSummaryRule {
    override fun check(item: RollingRateCardSummary): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class RollingRateCardSummaryAmountRangeRule(
    var min: Double,
    var max: Double,
) : RollingRateCardSummaryRule {
    override fun check(item: RollingRateCardSummary): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardSummaryCodePrefixRule(var prefix: String) : RollingRateCardSummaryRule {
    override fun check(item: RollingRateCardSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardSummaryPinnedGuardRule(var limit: Double) : RollingRateCardSummaryRule {
    override fun check(item: RollingRateCardSummary): MutableList<String> {
        if (item.pinned && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardSummaryCodeLengthRule(var maxLength: Int) : RollingRateCardSummaryRule {
    override fun check(item: RollingRateCardSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling rate card summary. */
data class RollingRateCardSummaryValidator(var rules: MutableList<RollingRateCardSummaryRule>) {
    fun validate(item: RollingRateCardSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingRateCardSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRateCardSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRateCardSummaryValidator(): RollingRateCardSummaryValidator {
    return RollingRateCardSummaryValidator(
        mutableListOf<RollingRateCardSummaryRule>(
            RollingRateCardSummaryBudgetRangeRule(0.5, 3302.2),
            RollingRateCardSummaryAmountRangeRule(9.3, 1572.3),
            RollingRateCardSummaryCodePrefixRule("REF"),
            RollingRateCardSummaryPinnedGuardRule(6.3),
            RollingRateCardSummaryCodeLengthRule(86400),
        ),
    )
}
