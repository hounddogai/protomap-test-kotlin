package com.acme.transport.emissions.carbonreport.score

/** A rule a regional carbon report score must pass. Check returns its problems, or none. */
interface RegionalCarbonReportScoreRule {
    fun check(item: RegionalCarbonReportScore): MutableList<String>
}

data class RegionalCarbonReportScoreWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : RegionalCarbonReportScoreRule {
    override fun check(item: RegionalCarbonReportScore): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportScoreCodePrefixRule(var prefix: String) : RegionalCarbonReportScoreRule {
    override fun check(item: RegionalCarbonReportScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportScoreRequiredTrailerIdRule(var message: String) : RegionalCarbonReportScoreRule {
    override fun check(item: RegionalCarbonReportScore): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportScoreLaneLengthRule(var maxLength: Int) : RegionalCarbonReportScoreRule {
    override fun check(item: RegionalCarbonReportScore): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportScoreMinimumRangeRule(
    var min: Double,
    var max: Double,
) : RegionalCarbonReportScoreRule {
    override fun check(item: RegionalCarbonReportScore): MutableList<String> {
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

/** Runs every rule of a regional carbon report score. */
data class RegionalCarbonReportScoreValidator(var rules: MutableList<RegionalCarbonReportScoreRule>) {
    fun validate(item: RegionalCarbonReportScore): MutableList<String> {
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

    fun requireValid(item: RegionalCarbonReportScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCarbonReportScore>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCarbonReportScoreValidator(): RegionalCarbonReportScoreValidator {
    return RegionalCarbonReportScoreValidator(
        mutableListOf<RegionalCarbonReportScoreRule>(
            RegionalCarbonReportScoreWindowDaysRangeRule(80, 86400),
            RegionalCarbonReportScoreCodePrefixRule("REF"),
            RegionalCarbonReportScoreRequiredTrailerIdRule("regional carbon report score needs an entry"),
            RegionalCarbonReportScoreLaneLengthRule(32),
            RegionalCarbonReportScoreMinimumRangeRule(7.5, 2589.6),
        ),
    )
}
