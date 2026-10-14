package com.acme.transport.crewing.roster.variance

/** A rule a strict roster mapping must pass. Check returns its problems, or none. */
interface StrictRosterMappingRule {
    fun check(item: StrictRosterMapping): MutableList<String>
}

data class StrictRosterMappingRequiredCountryRule(var message: String) : StrictRosterMappingRule {
    override fun check(item: StrictRosterMapping): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRosterMappingThresholdRangeRule(var min: Double, var max: Double) : StrictRosterMappingRule {
    override fun check(item: StrictRosterMapping): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class StrictRosterMappingCodePrefixRule(var prefix: String) : StrictRosterMappingRule {
    override fun check(item: StrictRosterMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictRosterMappingRatioRangeRule(var min: Double, var max: Double) : StrictRosterMappingRule {
    override fun check(item: StrictRosterMapping): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictRosterMappingRequiredCodeRule(var message: String) : StrictRosterMappingRule {
    override fun check(item: StrictRosterMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict roster mapping. */
data class StrictRosterMappingValidator(var rules: MutableList<StrictRosterMappingRule>) {
    fun validate(item: StrictRosterMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictRosterMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRosterMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRosterMappingValidator(): StrictRosterMappingValidator {
    return StrictRosterMappingValidator(
        mutableListOf<StrictRosterMappingRule>(
            StrictRosterMappingRequiredCountryRule("strict roster mapping needs this field"),
            StrictRosterMappingThresholdRangeRule(3.1, 2752.1),
            StrictRosterMappingCodePrefixRule("X"),
            StrictRosterMappingRatioRangeRule(10.0, 4322.4),
            StrictRosterMappingRequiredCodeRule("strict roster mapping needs a value"),
        ),
    )
}
