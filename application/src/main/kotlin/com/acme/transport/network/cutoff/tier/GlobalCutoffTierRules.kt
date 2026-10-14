package com.acme.transport.network.cutoff.tier

/** A rule a global cutoff tier must pass. Check returns its problems, or none. */
interface GlobalCutoffTierRule {
    fun check(item: GlobalCutoffTier): MutableList<String>
}

data class GlobalCutoffTierCreatedAtRangeRule(var min: Int, var max: Int) : GlobalCutoffTierRule {
    override fun check(item: GlobalCutoffTier): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class GlobalCutoffTierMaximumRangeRule(var min: Double, var max: Double) : GlobalCutoffTierRule {
    override fun check(item: GlobalCutoffTier): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class GlobalCutoffTierCodePrefixRule(var prefix: String) : GlobalCutoffTierRule {
    override fun check(item: GlobalCutoffTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCutoffTierBudgetRangeRule(var min: Double, var max: Double) : GlobalCutoffTierRule {
    override fun check(item: GlobalCutoffTier): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global cutoff tier. */
data class GlobalCutoffTierValidator(var rules: MutableList<GlobalCutoffTierRule>) {
    fun validate(item: GlobalCutoffTier): MutableList<String> {
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

    fun requireValid(item: GlobalCutoffTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCutoffTier>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCutoffTierValidator(): GlobalCutoffTierValidator {
    return GlobalCutoffTierValidator(
        mutableListOf<GlobalCutoffTierRule>(
            GlobalCutoffTierCreatedAtRangeRule(255, 86400),
            GlobalCutoffTierMaximumRangeRule(1.9, 795.2),
            GlobalCutoffTierCodePrefixRule("Q"),
            GlobalCutoffTierBudgetRangeRule(0.7, 3395.1),
        ),
    )
}
