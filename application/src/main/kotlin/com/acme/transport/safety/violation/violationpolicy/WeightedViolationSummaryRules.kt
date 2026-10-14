package com.acme.transport.safety.violation.violationpolicy

/** A rule a weighted violation summary must pass. Check returns its problems, or none. */
interface WeightedViolationSummaryRule {
    fun check(item: WeightedViolationSummary): MutableList<String>
}

data class WeightedViolationSummaryCodeLengthRule(var maxLength: Int) : WeightedViolationSummaryRule {
    override fun check(item: WeightedViolationSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedViolationSummaryBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : WeightedViolationSummaryRule {
    override fun check(item: WeightedViolationSummary): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class WeightedViolationSummaryRequiredCountryRule(var message: String) : WeightedViolationSummaryRule {
    override fun check(item: WeightedViolationSummary): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedViolationSummaryCodePrefixRule(var prefix: String) : WeightedViolationSummaryRule {
    override fun check(item: WeightedViolationSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted violation summary. */
data class WeightedViolationSummaryValidator(var rules: MutableList<WeightedViolationSummaryRule>) {
    fun validate(item: WeightedViolationSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedViolationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedViolationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedViolationSummaryValidator(): WeightedViolationSummaryValidator {
    return WeightedViolationSummaryValidator(
        mutableListOf<WeightedViolationSummaryRule>(
            WeightedViolationSummaryCodeLengthRule(16),
            WeightedViolationSummaryBatchSizeRangeRule(32, 3600),
            WeightedViolationSummaryRequiredCountryRule("weighted violation summary needs a value"),
            WeightedViolationSummaryCodePrefixRule("REF"),
        ),
    )
}
