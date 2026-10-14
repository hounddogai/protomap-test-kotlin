package com.acme.transport.network.hub.hubsummary

/** A rule a default hub tier must pass. Check returns its problems, or none. */
interface DefaultHubTierRule {
    fun check(item: DefaultHubTier): MutableList<String>
}

data class DefaultHubTierCodeLengthRule(var maxLength: Int) : DefaultHubTierRule {
    override fun check(item: DefaultHubTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubTierTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : DefaultHubTierRule {
    override fun check(item: DefaultHubTier): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class DefaultHubTierLaneLengthRule(var maxLength: Int) : DefaultHubTierRule {
    override fun check(item: DefaultHubTier): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubTierSequenceRangeRule(var min: Int, var max: Int) : DefaultHubTierRule {
    override fun check(item: DefaultHubTier): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default hub tier. */
data class DefaultHubTierValidator(var rules: MutableList<DefaultHubTierRule>) {
    fun validate(item: DefaultHubTier): MutableList<String> {
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

    fun requireValid(item: DefaultHubTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultHubTier>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultHubTierValidator(): DefaultHubTierValidator {
    return DefaultHubTierValidator(
        mutableListOf<DefaultHubTierRule>(
            DefaultHubTierCodeLengthRule(255),
            DefaultHubTierTagsLimitRule(86400, mutableListOf<String>("manual", "weighted", "rolling")),
            DefaultHubTierLaneLengthRule(86400),
            DefaultHubTierSequenceRangeRule(80, 3600),
        ),
    )
}
