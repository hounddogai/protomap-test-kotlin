package com.acme.transport.emissions.idling.summary

/** A rule a regional idling summary must pass. Check returns its problems, or none. */
interface RegionalIdlingSummaryRule {
    fun check(item: RegionalIdlingSummary): MutableList<String>
}

data class RegionalIdlingSummaryRequiredCodeRule(var message: String) : RegionalIdlingSummaryRule {
    override fun check(item: RegionalIdlingSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingSummaryNameLengthRule(var maxLength: Int) : RegionalIdlingSummaryRule {
    override fun check(item: RegionalIdlingSummary): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingSummaryRetriesRangeRule(var min: Int, var max: Int) : RegionalIdlingSummaryRule {
    override fun check(item: RegionalIdlingSummary): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingSummaryCodePrefixRule(var prefix: String) : RegionalIdlingSummaryRule {
    override fun check(item: RegionalIdlingSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional idling summary. */
data class RegionalIdlingSummaryValidator(var rules: MutableList<RegionalIdlingSummaryRule>) {
    fun validate(item: RegionalIdlingSummary): MutableList<String> {
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

    fun requireValid(item: RegionalIdlingSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalIdlingSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalIdlingSummaryValidator(): RegionalIdlingSummaryValidator {
    return RegionalIdlingSummaryValidator(
        mutableListOf<RegionalIdlingSummaryRule>(
            RegionalIdlingSummaryRequiredCodeRule("regional idling summary needs a value"),
            RegionalIdlingSummaryNameLengthRule(80),
            RegionalIdlingSummaryRetriesRangeRule(16, 1000),
            RegionalIdlingSummaryCodePrefixRule("ACM"),
        ),
    )
}
