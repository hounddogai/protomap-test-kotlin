package com.acme.transport.network.servicearea.areabatch

/** A rule a regional service area estimate must pass. Check returns its problems, or none. */
interface RegionalServiceAreaEstimateRule {
    fun check(item: RegionalServiceAreaEstimate): MutableList<String>
}

data class RegionalServiceAreaEstimateCodePrefixRule(var prefix: String) : RegionalServiceAreaEstimateRule {
    override fun check(item: RegionalServiceAreaEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaEstimateCountryLengthRule(var maxLength: Int) : RegionalServiceAreaEstimateRule {
    override fun check(item: RegionalServiceAreaEstimate): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaEstimateRequiredLabelRule(var message: String) : RegionalServiceAreaEstimateRule {
    override fun check(item: RegionalServiceAreaEstimate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaEstimateWeightRangeRule(
    var min: Double,
    var max: Double,
) : RegionalServiceAreaEstimateRule {
    override fun check(item: RegionalServiceAreaEstimate): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaEstimateCodeLengthRule(var maxLength: Int) : RegionalServiceAreaEstimateRule {
    override fun check(item: RegionalServiceAreaEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional service area estimate. */
data class RegionalServiceAreaEstimateValidator(var rules: MutableList<RegionalServiceAreaEstimateRule>) {
    fun validate(item: RegionalServiceAreaEstimate): MutableList<String> {
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

    fun requireValid(item: RegionalServiceAreaEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalServiceAreaEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalServiceAreaEstimateValidator(): RegionalServiceAreaEstimateValidator {
    return RegionalServiceAreaEstimateValidator(
        mutableListOf<RegionalServiceAreaEstimateRule>(
            RegionalServiceAreaEstimateCodePrefixRule("ACM"),
            RegionalServiceAreaEstimateCountryLengthRule(86400),
            RegionalServiceAreaEstimateRequiredLabelRule("regional service area estimate needs an entry"),
            RegionalServiceAreaEstimateWeightRangeRule(2.2, 990.7),
            RegionalServiceAreaEstimateCodeLengthRule(120),
        ),
    )
}
