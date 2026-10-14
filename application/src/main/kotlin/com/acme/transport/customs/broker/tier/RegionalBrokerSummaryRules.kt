package com.acme.transport.customs.broker.tier

/** A rule a regional broker summary must pass. Check returns its problems, or none. */
interface RegionalBrokerSummaryRule {
    fun check(item: RegionalBrokerSummary): MutableList<String>
}

data class RegionalBrokerSummaryUnitLengthRule(var maxLength: Int) : RegionalBrokerSummaryRule {
    override fun check(item: RegionalBrokerSummary): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerSummaryCodeLengthRule(var maxLength: Int) : RegionalBrokerSummaryRule {
    override fun check(item: RegionalBrokerSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerSummaryRequiredNameRule(var message: String) : RegionalBrokerSummaryRule {
    override fun check(item: RegionalBrokerSummary): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerSummaryDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : RegionalBrokerSummaryRule {
    override fun check(item: RegionalBrokerSummary): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional broker summary. */
data class RegionalBrokerSummaryValidator(var rules: MutableList<RegionalBrokerSummaryRule>) {
    fun validate(item: RegionalBrokerSummary): MutableList<String> {
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

    fun requireValid(item: RegionalBrokerSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalBrokerSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalBrokerSummaryValidator(): RegionalBrokerSummaryValidator {
    return RegionalBrokerSummaryValidator(
        mutableListOf<RegionalBrokerSummaryRule>(
            RegionalBrokerSummaryUnitLengthRule(1000),
            RegionalBrokerSummaryCodeLengthRule(86400),
            RegionalBrokerSummaryRequiredNameRule("regional broker summary needs a value"),
            RegionalBrokerSummaryDurationMinutesRangeRule(32, 1000),
        ),
    )
}
