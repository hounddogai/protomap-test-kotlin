package com.acme.transport.crewing.roster.rostersummary

/** A rule a weighted roster summary must pass. Check returns its problems, or none. */
interface WeightedRosterSummaryRule {
    fun check(item: WeightedRosterSummary): MutableList<String>
}

data class WeightedRosterSummaryLabelLengthRule(var maxLength: Int) : WeightedRosterSummaryRule {
    override fun check(item: WeightedRosterSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRosterSummaryRequiredSegmentRule(var message: String) : WeightedRosterSummaryRule {
    override fun check(item: WeightedRosterSummary): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRosterSummaryVersionRangeRule(var min: Int, var max: Int) : WeightedRosterSummaryRule {
    override fun check(item: WeightedRosterSummary): MutableList<String> {
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

data class WeightedRosterSummaryCodePrefixRule(var prefix: String) : WeightedRosterSummaryRule {
    override fun check(item: WeightedRosterSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted roster summary. */
data class WeightedRosterSummaryValidator(var rules: MutableList<WeightedRosterSummaryRule>) {
    fun validate(item: WeightedRosterSummary): MutableList<String> {
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

    fun requireValid(item: WeightedRosterSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRosterSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRosterSummaryValidator(): WeightedRosterSummaryValidator {
    return WeightedRosterSummaryValidator(
        mutableListOf<WeightedRosterSummaryRule>(
            WeightedRosterSummaryLabelLengthRule(80),
            WeightedRosterSummaryRequiredSegmentRule("weighted roster summary needs an entry"),
            WeightedRosterSummaryVersionRangeRule(255, 1000),
            WeightedRosterSummaryCodePrefixRule("X"),
        ),
    )
}
