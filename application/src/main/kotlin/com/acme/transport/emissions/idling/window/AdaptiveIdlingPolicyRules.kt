package com.acme.transport.emissions.idling.window

/** A rule a adaptive idling policy must pass. Check returns its problems, or none. */
interface AdaptiveIdlingPolicyRule {
    fun check(item: AdaptiveIdlingPolicy): MutableList<String>
}

data class AdaptiveIdlingPolicyBatchSizeRangeRule(var min: Int, var max: Int) : AdaptiveIdlingPolicyRule {
    override fun check(item: AdaptiveIdlingPolicy): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingPolicyStartsAtRangeRule(var min: Int, var max: Int) : AdaptiveIdlingPolicyRule {
    override fun check(item: AdaptiveIdlingPolicy): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingPolicyCodePrefixRule(var prefix: String) : AdaptiveIdlingPolicyRule {
    override fun check(item: AdaptiveIdlingPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive idling policy. */
data class AdaptiveIdlingPolicyValidator(var rules: MutableList<AdaptiveIdlingPolicyRule>) {
    fun validate(item: AdaptiveIdlingPolicy): MutableList<String> {
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

    fun requireValid(item: AdaptiveIdlingPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveIdlingPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveIdlingPolicyValidator(): AdaptiveIdlingPolicyValidator {
    return AdaptiveIdlingPolicyValidator(
        mutableListOf<AdaptiveIdlingPolicyRule>(
            AdaptiveIdlingPolicyBatchSizeRangeRule(32, 64),
            AdaptiveIdlingPolicyStartsAtRangeRule(32, 64),
            AdaptiveIdlingPolicyCodePrefixRule("ACM"),
        ),
    )
}
