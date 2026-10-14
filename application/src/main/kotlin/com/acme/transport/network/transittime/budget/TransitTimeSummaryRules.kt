package com.acme.transport.network.transittime.budget

/** A rule a transit time summary must pass. Check returns its problems, or none. */
interface TransitTimeSummaryRule {
    fun check(item: TransitTimeSummary): MutableList<String>
}

data class TransitTimeSummaryCodePrefixRule(var prefix: String) : TransitTimeSummaryRule {
    override fun check(item: TransitTimeSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeSummaryPalletsRangeRule(var min: Int, var max: Int) : TransitTimeSummaryRule {
    override fun check(item: TransitTimeSummary): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class TransitTimeSummaryRequiredNameRule(var message: String) : TransitTimeSummaryRule {
    override fun check(item: TransitTimeSummary): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeSummaryUpdatedAtRangeRule(var min: Int, var max: Int) : TransitTimeSummaryRule {
    override fun check(item: TransitTimeSummary): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeSummaryRequiredTitleRule(var message: String) : TransitTimeSummaryRule {
    override fun check(item: TransitTimeSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time summary. */
data class TransitTimeSummaryValidator(var rules: MutableList<TransitTimeSummaryRule>) {
    fun validate(item: TransitTimeSummary): MutableList<String> {
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

    fun requireValid(item: TransitTimeSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeSummaryValidator(): TransitTimeSummaryValidator {
    return TransitTimeSummaryValidator(
        mutableListOf<TransitTimeSummaryRule>(
            TransitTimeSummaryCodePrefixRule("Q"),
            TransitTimeSummaryPalletsRangeRule(16, 1000),
            TransitTimeSummaryRequiredNameRule("transit time summary needs a value"),
            TransitTimeSummaryUpdatedAtRangeRule(64, 86400),
            TransitTimeSummaryRequiredTitleRule("transit time summary needs this field"),
        ),
    )
}
