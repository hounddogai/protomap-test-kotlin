package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a adaptive swap score must pass. Check returns its problems, or none. */
interface AdaptiveSwapScoreRule {
    fun check(item: AdaptiveSwapScore): MutableList<String>
}

data class AdaptiveSwapScoreLabelLengthRule(var maxLength: Int) : AdaptiveSwapScoreRule {
    override fun check(item: AdaptiveSwapScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapScoreAllowedCodeRule(var allowed: MutableList<String>) : AdaptiveSwapScoreRule {
    override fun check(item: AdaptiveSwapScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapScoreCostRangeRule(var min: Double, var max: Double) : AdaptiveSwapScoreRule {
    override fun check(item: AdaptiveSwapScore): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapScoreRequiredCodeRule(var message: String) : AdaptiveSwapScoreRule {
    override fun check(item: AdaptiveSwapScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapScoreEndsAtRangeRule(var min: Int, var max: Int) : AdaptiveSwapScoreRule {
    override fun check(item: AdaptiveSwapScore): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive swap score. */
data class AdaptiveSwapScoreValidator(var rules: MutableList<AdaptiveSwapScoreRule>) {
    fun validate(item: AdaptiveSwapScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveSwapScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveSwapScore>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveSwapScoreValidator(): AdaptiveSwapScoreValidator {
    return AdaptiveSwapScoreValidator(
        mutableListOf<AdaptiveSwapScoreRule>(
            AdaptiveSwapScoreLabelLengthRule(16),
            AdaptiveSwapScoreAllowedCodeRule(mutableListOf<String>("weighted", "adaptive", "global")),
            AdaptiveSwapScoreCostRangeRule(9.8, 1095.6),
            AdaptiveSwapScoreRequiredCodeRule("adaptive swap score needs this field"),
            AdaptiveSwapScoreEndsAtRangeRule(32, 3600),
        ),
    )
}
