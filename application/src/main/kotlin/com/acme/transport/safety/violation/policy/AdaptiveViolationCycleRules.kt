package com.acme.transport.safety.violation.policy

/** A rule a adaptive violation cycle must pass. Check returns its problems, or none. */
interface AdaptiveViolationCycleRule {
    fun check(item: AdaptiveViolationCycle): MutableList<String>
}

data class AdaptiveViolationCycleCapacityRangeRule(var min: Int, var max: Int) : AdaptiveViolationCycleRule {
    override fun check(item: AdaptiveViolationCycle): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class AdaptiveViolationCycleVersionRangeRule(var min: Int, var max: Int) : AdaptiveViolationCycleRule {
    override fun check(item: AdaptiveViolationCycle): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class AdaptiveViolationCycleCodePrefixRule(var prefix: String) : AdaptiveViolationCycleRule {
    override fun check(item: AdaptiveViolationCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive violation cycle. */
data class AdaptiveViolationCycleValidator(var rules: MutableList<AdaptiveViolationCycleRule>) {
    fun validate(item: AdaptiveViolationCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveViolationCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveViolationCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveViolationCycleValidator(): AdaptiveViolationCycleValidator {
    return AdaptiveViolationCycleValidator(
        mutableListOf<AdaptiveViolationCycleRule>(
            AdaptiveViolationCycleCapacityRangeRule(16, 80),
            AdaptiveViolationCycleVersionRangeRule(64, 64),
            AdaptiveViolationCycleCodePrefixRule("X"),
        ),
    )
}
