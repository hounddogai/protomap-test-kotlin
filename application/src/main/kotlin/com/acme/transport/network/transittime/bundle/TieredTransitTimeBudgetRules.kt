package com.acme.transport.network.transittime.bundle

/** A rule a tiered transit time budget must pass. Check returns its problems, or none. */
interface TieredTransitTimeBudgetRule {
    fun check(item: TieredTransitTimeBudget): MutableList<String>
}

data class TieredTransitTimeBudgetVersionRangeRule(var min: Int, var max: Int) : TieredTransitTimeBudgetRule {
    override fun check(item: TieredTransitTimeBudget): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredTransitTimeBudgetTimezoneLengthRule(var maxLength: Int) : TieredTransitTimeBudgetRule {
    override fun check(item: TieredTransitTimeBudget): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTransitTimeBudgetCodePrefixRule(var prefix: String) : TieredTransitTimeBudgetRule {
    override fun check(item: TieredTransitTimeBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredTransitTimeBudgetRequiredCodeRule(var message: String) : TieredTransitTimeBudgetRule {
    override fun check(item: TieredTransitTimeBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTransitTimeBudgetApprovedGuardRule(var limit: Int) : TieredTransitTimeBudgetRule {
    override fun check(item: TieredTransitTimeBudget): MutableList<String> {
        if (item.approved && item.version > limit) {
            return mutableListOf<String>("version ${item.version} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class TieredTransitTimeBudgetCodeLengthRule(var maxLength: Int) : TieredTransitTimeBudgetRule {
    override fun check(item: TieredTransitTimeBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered transit time budget. */
data class TieredTransitTimeBudgetValidator(var rules: MutableList<TieredTransitTimeBudgetRule>) {
    fun validate(item: TieredTransitTimeBudget): MutableList<String> {
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

    fun requireValid(item: TieredTransitTimeBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTransitTimeBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTransitTimeBudgetValidator(): TieredTransitTimeBudgetValidator {
    return TieredTransitTimeBudgetValidator(
        mutableListOf<TieredTransitTimeBudgetRule>(
            TieredTransitTimeBudgetVersionRangeRule(3600, 3600),
            TieredTransitTimeBudgetTimezoneLengthRule(86400),
            TieredTransitTimeBudgetCodePrefixRule("REF"),
            TieredTransitTimeBudgetRequiredCodeRule("tiered transit time budget needs a value"),
            TieredTransitTimeBudgetApprovedGuardRule(86400),
            TieredTransitTimeBudgetCodeLengthRule(255),
        ),
    )
}
