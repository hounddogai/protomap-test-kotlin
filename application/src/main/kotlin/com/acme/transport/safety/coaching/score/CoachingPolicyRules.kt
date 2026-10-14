package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.text.normalize

/** A rule a coaching policy must pass. Check returns its problems, or none. */
interface CoachingPolicyRule {
    fun check(item: CoachingPolicy): MutableList<String>
}

data class CoachingPolicyRequiredCodeRule(var message: String) : CoachingPolicyRule {
    override fun check(item: CoachingPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingPolicyAllowedCategoryRule(var allowed: MutableList<String>) : CoachingPolicyRule {
    override fun check(item: CoachingPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CoachingPolicyCodePrefixRule(var prefix: String) : CoachingPolicyRule {
    override fun check(item: CoachingPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CoachingPolicyFuelLitersRangeRule(var min: Double, var max: Double) : CoachingPolicyRule {
    override fun check(item: CoachingPolicy): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class CoachingPolicyLaneLengthRule(var maxLength: Int) : CoachingPolicyRule {
    override fun check(item: CoachingPolicy): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingPolicyNameLengthRule(var maxLength: Int) : CoachingPolicyRule {
    override fun check(item: CoachingPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching policy. */
data class CoachingPolicyValidator(var rules: MutableList<CoachingPolicyRule>) {
    fun validate(item: CoachingPolicy): MutableList<String> {
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

    fun requireValid(item: CoachingPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingPolicyValidator(): CoachingPolicyValidator {
    return CoachingPolicyValidator(
        mutableListOf<CoachingPolicyRule>(
            CoachingPolicyRequiredCodeRule("coaching policy needs an entry"),
            CoachingPolicyAllowedCategoryRule(mutableListOf<String>()),
            CoachingPolicyCodePrefixRule("ACM"),
            CoachingPolicyFuelLitersRangeRule(8.1, 1414.4),
            CoachingPolicyLaneLengthRule(1000),
            CoachingPolicyNameLengthRule(80),
        ),
    )
}
