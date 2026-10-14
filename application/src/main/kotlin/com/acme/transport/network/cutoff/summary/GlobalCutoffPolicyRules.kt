package com.acme.transport.network.cutoff.summary

/** A rule a global cutoff policy must pass. Check returns its problems, or none. */
interface GlobalCutoffPolicyRule {
    fun check(item: GlobalCutoffPolicy): MutableList<String>
}

data class GlobalCutoffPolicyDiscountRangeRule(var min: Double, var max: Double) : GlobalCutoffPolicyRule {
    override fun check(item: GlobalCutoffPolicy): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCutoffPolicyAttemptsRangeRule(var min: Int, var max: Int) : GlobalCutoffPolicyRule {
    override fun check(item: GlobalCutoffPolicy): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class GlobalCutoffPolicyContactEmailLengthRule(var maxLength: Int) : GlobalCutoffPolicyRule {
    override fun check(item: GlobalCutoffPolicy): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global cutoff policy. */
data class GlobalCutoffPolicyValidator(var rules: MutableList<GlobalCutoffPolicyRule>) {
    fun validate(item: GlobalCutoffPolicy): MutableList<String> {
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

    fun requireValid(item: GlobalCutoffPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCutoffPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCutoffPolicyValidator(): GlobalCutoffPolicyValidator {
    return GlobalCutoffPolicyValidator(
        mutableListOf<GlobalCutoffPolicyRule>(
            GlobalCutoffPolicyDiscountRangeRule(5.3, 1738.2),
            GlobalCutoffPolicyAttemptsRangeRule(32, 1000),
            GlobalCutoffPolicyContactEmailLengthRule(32),
        ),
    )
}
