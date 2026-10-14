package com.acme.transport.safety.coaching.entry

/** A rule a regional coaching estimate must pass. Check returns its problems, or none. */
interface RegionalCoachingEstimateRule {
    fun check(item: RegionalCoachingEstimate): MutableList<String>
}

data class RegionalCoachingEstimateRequiredCodeRule(var message: String) : RegionalCoachingEstimateRule {
    override fun check(item: RegionalCoachingEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingEstimateNameLengthRule(var maxLength: Int) : RegionalCoachingEstimateRule {
    override fun check(item: RegionalCoachingEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingEstimateStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : RegionalCoachingEstimateRule {
    override fun check(item: RegionalCoachingEstimate): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingEstimateCodePrefixRule(var prefix: String) : RegionalCoachingEstimateRule {
    override fun check(item: RegionalCoachingEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingEstimateCodeLengthRule(var maxLength: Int) : RegionalCoachingEstimateRule {
    override fun check(item: RegionalCoachingEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingEstimateThresholdRangeRule(
    var min: Double,
    var max: Double,
) : RegionalCoachingEstimateRule {
    override fun check(item: RegionalCoachingEstimate): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional coaching estimate. */
data class RegionalCoachingEstimateValidator(var rules: MutableList<RegionalCoachingEstimateRule>) {
    fun validate(item: RegionalCoachingEstimate): MutableList<String> {
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

    fun requireValid(item: RegionalCoachingEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCoachingEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCoachingEstimateValidator(): RegionalCoachingEstimateValidator {
    return RegionalCoachingEstimateValidator(
        mutableListOf<RegionalCoachingEstimateRule>(
            RegionalCoachingEstimateRequiredCodeRule("regional coaching estimate needs a value"),
            RegionalCoachingEstimateNameLengthRule(255),
            RegionalCoachingEstimateStartsAtRangeRule(16, 80),
            RegionalCoachingEstimateCodePrefixRule("REF"),
            RegionalCoachingEstimateCodeLengthRule(1000),
            RegionalCoachingEstimateThresholdRangeRule(4.9, 3021.9),
        ),
    )
}
