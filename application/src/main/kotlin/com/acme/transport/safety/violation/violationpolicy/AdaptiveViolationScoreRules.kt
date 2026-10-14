package com.acme.transport.safety.violation.violationpolicy

/** A rule a adaptive violation score must pass. Check returns its problems, or none. */
interface AdaptiveViolationScoreRule {
    fun check(item: AdaptiveViolationScore): MutableList<String>
}

data class AdaptiveViolationScoreCodePrefixRule(var prefix: String) : AdaptiveViolationScoreRule {
    override fun check(item: AdaptiveViolationScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveViolationScoreDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveViolationScoreRule {
    override fun check(item: AdaptiveViolationScore): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveViolationScoreRequiredLaneRule(var message: String) : AdaptiveViolationScoreRule {
    override fun check(item: AdaptiveViolationScore): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveViolationScoreLaneLengthRule(var maxLength: Int) : AdaptiveViolationScoreRule {
    override fun check(item: AdaptiveViolationScore): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveViolationScoreRequiredCurrencyRule(var message: String) : AdaptiveViolationScoreRule {
    override fun check(item: AdaptiveViolationScore): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveViolationScoreRequiredCodeRule(var message: String) : AdaptiveViolationScoreRule {
    override fun check(item: AdaptiveViolationScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive violation score. */
data class AdaptiveViolationScoreValidator(var rules: MutableList<AdaptiveViolationScoreRule>) {
    fun validate(item: AdaptiveViolationScore): MutableList<String> {
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

    fun requireValid(item: AdaptiveViolationScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveViolationScore>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveViolationScoreValidator(): AdaptiveViolationScoreValidator {
    return AdaptiveViolationScoreValidator(
        mutableListOf<AdaptiveViolationScoreRule>(
            AdaptiveViolationScoreCodePrefixRule("X"),
            AdaptiveViolationScoreDistanceKmRangeRule(4.2, 2141.7),
            AdaptiveViolationScoreRequiredLaneRule("adaptive violation score needs this field"),
            AdaptiveViolationScoreLaneLengthRule(64),
            AdaptiveViolationScoreRequiredCurrencyRule("adaptive violation score needs a value"),
            AdaptiveViolationScoreRequiredCodeRule("adaptive violation score needs a value"),
        ),
    )
}
