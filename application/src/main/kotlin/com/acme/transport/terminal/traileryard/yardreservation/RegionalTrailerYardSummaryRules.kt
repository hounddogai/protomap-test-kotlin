package com.acme.transport.terminal.traileryard.yardreservation

/** A rule a regional trailer yard summary must pass. Check returns its problems, or none. */
interface RegionalTrailerYardSummaryRule {
    fun check(item: RegionalTrailerYardSummary): MutableList<String>
}

data class RegionalTrailerYardSummaryDriverIdLengthRule(var maxLength: Int) : RegionalTrailerYardSummaryRule {
    override fun check(item: RegionalTrailerYardSummary): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerYardSummaryVersionRangeRule(
    var min: Int,
    var max: Int,
) : RegionalTrailerYardSummaryRule {
    override fun check(item: RegionalTrailerYardSummary): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerYardSummaryRequiredDriverIdRule(
    var message: String,
) : RegionalTrailerYardSummaryRule {
    override fun check(item: RegionalTrailerYardSummary): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional trailer yard summary. */
data class RegionalTrailerYardSummaryValidator(var rules: MutableList<RegionalTrailerYardSummaryRule>) {
    fun validate(item: RegionalTrailerYardSummary): MutableList<String> {
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

    fun requireValid(item: RegionalTrailerYardSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTrailerYardSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTrailerYardSummaryValidator(): RegionalTrailerYardSummaryValidator {
    return RegionalTrailerYardSummaryValidator(
        mutableListOf<RegionalTrailerYardSummaryRule>(
            RegionalTrailerYardSummaryDriverIdLengthRule(255),
            RegionalTrailerYardSummaryVersionRangeRule(64, 1000),
            RegionalTrailerYardSummaryRequiredDriverIdRule("regional trailer yard summary needs a value"),
        ),
    )
}
