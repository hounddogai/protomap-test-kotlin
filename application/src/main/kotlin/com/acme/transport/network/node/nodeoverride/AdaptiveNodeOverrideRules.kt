package com.acme.transport.network.node.nodeoverride

/** A rule a adaptive node override must pass. Check returns its problems, or none. */
interface AdaptiveNodeOverrideRule {
    fun check(item: AdaptiveNodeOverride): MutableList<String>
}

data class AdaptiveNodeOverrideStartsAtRangeRule(var min: Int, var max: Int) : AdaptiveNodeOverrideRule {
    override fun check(item: AdaptiveNodeOverride): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class AdaptiveNodeOverrideTitleLengthRule(var maxLength: Int) : AdaptiveNodeOverrideRule {
    override fun check(item: AdaptiveNodeOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeOverrideLoadKgRangeRule(var min: Double, var max: Double) : AdaptiveNodeOverrideRule {
    override fun check(item: AdaptiveNodeOverride): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive node override. */
data class AdaptiveNodeOverrideValidator(var rules: MutableList<AdaptiveNodeOverrideRule>) {
    fun validate(item: AdaptiveNodeOverride): MutableList<String> {
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

    fun requireValid(item: AdaptiveNodeOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveNodeOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveNodeOverrideValidator(): AdaptiveNodeOverrideValidator {
    return AdaptiveNodeOverrideValidator(
        mutableListOf<AdaptiveNodeOverrideRule>(
            AdaptiveNodeOverrideStartsAtRangeRule(64, 3600),
            AdaptiveNodeOverrideTitleLengthRule(120),
            AdaptiveNodeOverrideLoadKgRangeRule(1.3, 434.4),
        ),
    )
}
