package com.acme.transport.customs.broker.allocation

/** A rule a seasonal broker summary must pass. Check returns its problems, or none. */
interface SeasonalBrokerSummaryRule {
    fun check(item: SeasonalBrokerSummary): MutableList<String>
}

data class SeasonalBrokerSummaryNameLengthRule(var maxLength: Int) : SeasonalBrokerSummaryRule {
    override fun check(item: SeasonalBrokerSummary): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalBrokerSummaryRequiredCodeRule(var message: String) : SeasonalBrokerSummaryRule {
    override fun check(item: SeasonalBrokerSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalBrokerSummaryVersionRangeRule(var min: Int, var max: Int) : SeasonalBrokerSummaryRule {
    override fun check(item: SeasonalBrokerSummary): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal broker summary. */
data class SeasonalBrokerSummaryValidator(var rules: MutableList<SeasonalBrokerSummaryRule>) {
    fun validate(item: SeasonalBrokerSummary): MutableList<String> {
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

    fun requireValid(item: SeasonalBrokerSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalBrokerSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalBrokerSummaryValidator(): SeasonalBrokerSummaryValidator {
    return SeasonalBrokerSummaryValidator(
        mutableListOf<SeasonalBrokerSummaryRule>(
            SeasonalBrokerSummaryNameLengthRule(86400),
            SeasonalBrokerSummaryRequiredCodeRule("seasonal broker summary needs this field"),
            SeasonalBrokerSummaryVersionRangeRule(16, 32),
        ),
    )
}
