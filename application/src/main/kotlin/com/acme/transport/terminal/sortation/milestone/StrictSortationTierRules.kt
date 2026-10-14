package com.acme.transport.terminal.sortation.milestone

/** A rule a strict sortation tier must pass. Check returns its problems, or none. */
interface StrictSortationTierRule {
    fun check(item: StrictSortationTier): MutableList<String>
}

data class StrictSortationTierActiveGuardRule(var limit: Int) : StrictSortationTierRule {
    override fun check(item: StrictSortationTier): MutableList<String> {
        if (item.active && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class StrictSortationTierCodeLengthRule(var maxLength: Int) : StrictSortationTierRule {
    override fun check(item: StrictSortationTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictSortationTierCodePrefixRule(var prefix: String) : StrictSortationTierRule {
    override fun check(item: StrictSortationTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictSortationTierRatioRangeRule(var min: Double, var max: Double) : StrictSortationTierRule {
    override fun check(item: StrictSortationTier): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class StrictSortationTierRequiredTitleRule(var message: String) : StrictSortationTierRule {
    override fun check(item: StrictSortationTier): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict sortation tier. */
data class StrictSortationTierValidator(var rules: MutableList<StrictSortationTierRule>) {
    fun validate(item: StrictSortationTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictSortationTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictSortationTier>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictSortationTierValidator(): StrictSortationTierValidator {
    return StrictSortationTierValidator(
        mutableListOf<StrictSortationTierRule>(
            StrictSortationTierActiveGuardRule(86400),
            StrictSortationTierCodeLengthRule(120),
            StrictSortationTierCodePrefixRule("ACM"),
            StrictSortationTierRatioRangeRule(4.8, 1072.9),
            StrictSortationTierRequiredTitleRule("strict sortation tier needs this field"),
        ),
    )
}
