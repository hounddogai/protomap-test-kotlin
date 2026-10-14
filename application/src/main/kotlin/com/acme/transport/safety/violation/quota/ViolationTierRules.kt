package com.acme.transport.safety.violation.quota

/** A rule a violation tier must pass. Check returns its problems, or none. */
interface ViolationTierRule {
    fun check(item: ViolationTier): MutableList<String>
}

data class ViolationTierCodePrefixRule(var prefix: String) : ViolationTierRule {
    override fun check(item: ViolationTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ViolationTierCreatedAtRangeRule(var min: Int, var max: Int) : ViolationTierRule {
    override fun check(item: ViolationTier): MutableList<String> {
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

data class ViolationTierBatchSizeRangeRule(var min: Int, var max: Int) : ViolationTierRule {
    override fun check(item: ViolationTier): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation tier. */
data class ViolationTierValidator(var rules: MutableList<ViolationTierRule>) {
    fun validate(item: ViolationTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationTier>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationTierValidator(): ViolationTierValidator {
    return ViolationTierValidator(
        mutableListOf<ViolationTierRule>(
            ViolationTierCodePrefixRule("ACM"),
            ViolationTierCreatedAtRangeRule(32, 1000),
            ViolationTierBatchSizeRangeRule(64, 86400),
        ),
    )
}
