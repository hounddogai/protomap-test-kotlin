package com.acme.transport.terminal.dwell.summary

/** A rule a weighted dwell summary must pass. Check returns its problems, or none. */
interface WeightedDwellSummaryRule {
    fun check(item: WeightedDwellSummary): MutableList<String>
}

data class WeightedDwellSummaryRequiredLabelRule(var message: String) : WeightedDwellSummaryRule {
    override fun check(item: WeightedDwellSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDwellSummaryCodePrefixRule(var prefix: String) : WeightedDwellSummaryRule {
    override fun check(item: WeightedDwellSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDwellSummaryTrailerIdLengthRule(var maxLength: Int) : WeightedDwellSummaryRule {
    override fun check(item: WeightedDwellSummary): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDwellSummaryLabelLengthRule(var maxLength: Int) : WeightedDwellSummaryRule {
    override fun check(item: WeightedDwellSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDwellSummaryRequiredCodeRule(var message: String) : WeightedDwellSummaryRule {
    override fun check(item: WeightedDwellSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDwellSummaryRetriesRangeRule(var min: Int, var max: Int) : WeightedDwellSummaryRule {
    override fun check(item: WeightedDwellSummary): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted dwell summary. */
data class WeightedDwellSummaryValidator(var rules: MutableList<WeightedDwellSummaryRule>) {
    fun validate(item: WeightedDwellSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedDwellSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDwellSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDwellSummaryValidator(): WeightedDwellSummaryValidator {
    return WeightedDwellSummaryValidator(
        mutableListOf<WeightedDwellSummaryRule>(
            WeightedDwellSummaryRequiredLabelRule("weighted dwell summary needs a value"),
            WeightedDwellSummaryCodePrefixRule("ACM"),
            WeightedDwellSummaryTrailerIdLengthRule(16),
            WeightedDwellSummaryLabelLengthRule(255),
            WeightedDwellSummaryRequiredCodeRule("weighted dwell summary needs a value"),
            WeightedDwellSummaryRetriesRangeRule(1000, 86400),
        ),
    )
}
