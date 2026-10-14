package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.text.normalize

/** A rule a weighted near miss estimate must pass. Check returns its problems, or none. */
interface WeightedNearMissEstimateRule {
    fun check(item: WeightedNearMissEstimate): MutableList<String>
}

data class WeightedNearMissEstimateFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : WeightedNearMissEstimateRule {
    override fun check(item: WeightedNearMissEstimate): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedNearMissEstimateAllowedTitleRule(
    var allowed: MutableList<String>,
) : WeightedNearMissEstimateRule {
    override fun check(item: WeightedNearMissEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class WeightedNearMissEstimateLocaleLengthRule(var maxLength: Int) : WeightedNearMissEstimateRule {
    override fun check(item: WeightedNearMissEstimate): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedNearMissEstimateRequiredCodeRule(var message: String) : WeightedNearMissEstimateRule {
    override fun check(item: WeightedNearMissEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedNearMissEstimateReviewerLengthRule(var maxLength: Int) : WeightedNearMissEstimateRule {
    override fun check(item: WeightedNearMissEstimate): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedNearMissEstimateCodePrefixRule(var prefix: String) : WeightedNearMissEstimateRule {
    override fun check(item: WeightedNearMissEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted near miss estimate. */
data class WeightedNearMissEstimateValidator(var rules: MutableList<WeightedNearMissEstimateRule>) {
    fun validate(item: WeightedNearMissEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedNearMissEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedNearMissEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedNearMissEstimateValidator(): WeightedNearMissEstimateValidator {
    return WeightedNearMissEstimateValidator(
        mutableListOf<WeightedNearMissEstimateRule>(
            WeightedNearMissEstimateFuelLitersRangeRule(2.1, 479.9),
            WeightedNearMissEstimateAllowedTitleRule(mutableListOf<String>()),
            WeightedNearMissEstimateLocaleLengthRule(32),
            WeightedNearMissEstimateRequiredCodeRule("weighted near miss estimate needs an entry"),
            WeightedNearMissEstimateReviewerLengthRule(120),
            WeightedNearMissEstimateCodePrefixRule("ACM"),
        ),
    )
}
