package com.acme.transport.network.node.ledger

import com.acme.transport.kit.text.normalize

/** A rule a seasonal node bundle must pass. Check returns its problems, or none. */
interface SeasonalNodeBundleRule {
    fun check(item: SeasonalNodeBundle): MutableList<String>
}

data class SeasonalNodeBundleCodePrefixRule(var prefix: String) : SeasonalNodeBundleRule {
    override fun check(item: SeasonalNodeBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNodeBundleTitleLengthRule(var maxLength: Int) : SeasonalNodeBundleRule {
    override fun check(item: SeasonalNodeBundle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNodeBundleRequiredTitleRule(var message: String) : SeasonalNodeBundleRule {
    override fun check(item: SeasonalNodeBundle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNodeBundleAllowedTitleRule(var allowed: MutableList<String>) : SeasonalNodeBundleRule {
    override fun check(item: SeasonalNodeBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNodeBundleWeightRangeRule(var min: Double, var max: Double) : SeasonalNodeBundleRule {
    override fun check(item: SeasonalNodeBundle): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal node bundle. */
data class SeasonalNodeBundleValidator(var rules: MutableList<SeasonalNodeBundleRule>) {
    fun validate(item: SeasonalNodeBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalNodeBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalNodeBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalNodeBundleValidator(): SeasonalNodeBundleValidator {
    return SeasonalNodeBundleValidator(
        mutableListOf<SeasonalNodeBundleRule>(
            SeasonalNodeBundleCodePrefixRule("Q"),
            SeasonalNodeBundleTitleLengthRule(86400),
            SeasonalNodeBundleRequiredTitleRule("seasonal node bundle needs a value"),
            SeasonalNodeBundleAllowedTitleRule(mutableListOf<String>("lenient", "weighted", "rolling")),
            SeasonalNodeBundleWeightRangeRule(5.7, 2708.3),
        ),
    )
}
