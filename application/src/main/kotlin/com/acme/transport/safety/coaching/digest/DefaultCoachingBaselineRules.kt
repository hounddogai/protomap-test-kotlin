package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.text.normalize

/** A rule a default coaching baseline must pass. Check returns its problems, or none. */
interface DefaultCoachingBaselineRule {
    fun check(item: DefaultCoachingBaseline): MutableList<String>
}

data class DefaultCoachingBaselineVisibleGuardRule(var limit: Int) : DefaultCoachingBaselineRule {
    override fun check(item: DefaultCoachingBaseline): MutableList<String> {
        if (item.visible && item.pallets > limit) {
            return mutableListOf<String>("pallets ${item.pallets} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingBaselineMinimumRangeRule(
    var min: Double,
    var max: Double,
) : DefaultCoachingBaselineRule {
    override fun check(item: DefaultCoachingBaseline): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class DefaultCoachingBaselineCodeLengthRule(var maxLength: Int) : DefaultCoachingBaselineRule {
    override fun check(item: DefaultCoachingBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingBaselineAllowedPhoneRule(
    var allowed: MutableList<String>,
) : DefaultCoachingBaselineRule {
    override fun check(item: DefaultCoachingBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is expired")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingBaselineCodePrefixRule(var prefix: String) : DefaultCoachingBaselineRule {
    override fun check(item: DefaultCoachingBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default coaching baseline. */
data class DefaultCoachingBaselineValidator(var rules: MutableList<DefaultCoachingBaselineRule>) {
    fun validate(item: DefaultCoachingBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultCoachingBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCoachingBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCoachingBaselineValidator(): DefaultCoachingBaselineValidator {
    return DefaultCoachingBaselineValidator(
        mutableListOf<DefaultCoachingBaselineRule>(
            DefaultCoachingBaselineVisibleGuardRule(16),
            DefaultCoachingBaselineMinimumRangeRule(7.6, 3193.9),
            DefaultCoachingBaselineCodeLengthRule(120),
            DefaultCoachingBaselineAllowedPhoneRule(mutableListOf<String>("fallback")),
            DefaultCoachingBaselineCodePrefixRule("REF"),
        ),
    )
}
