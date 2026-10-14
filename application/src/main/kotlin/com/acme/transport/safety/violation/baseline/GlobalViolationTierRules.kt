package com.acme.transport.safety.violation.baseline

/** A rule a global violation tier must pass. Check returns its problems, or none. */
interface GlobalViolationTierRule {
    fun check(item: GlobalViolationTier): MutableList<String>
}

data class GlobalViolationTierEndsAtRangeRule(var min: Int, var max: Int) : GlobalViolationTierRule {
    override fun check(item: GlobalViolationTier): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationTierDescriptionLengthRule(var maxLength: Int) : GlobalViolationTierRule {
    override fun check(item: GlobalViolationTier): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationTierCodePrefixRule(var prefix: String) : GlobalViolationTierRule {
    override fun check(item: GlobalViolationTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global violation tier. */
data class GlobalViolationTierValidator(var rules: MutableList<GlobalViolationTierRule>) {
    fun validate(item: GlobalViolationTier): MutableList<String> {
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

    fun requireValid(item: GlobalViolationTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalViolationTier>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalViolationTierValidator(): GlobalViolationTierValidator {
    return GlobalViolationTierValidator(
        mutableListOf<GlobalViolationTierRule>(
            GlobalViolationTierEndsAtRangeRule(16, 32),
            GlobalViolationTierDescriptionLengthRule(80),
            GlobalViolationTierCodePrefixRule("Q"),
        ),
    )
}
