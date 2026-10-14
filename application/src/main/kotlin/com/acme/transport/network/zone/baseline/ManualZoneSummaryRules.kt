package com.acme.transport.network.zone.baseline

/** A rule a manual zone summary must pass. Check returns its problems, or none. */
interface ManualZoneSummaryRule {
    fun check(item: ManualZoneSummary): MutableList<String>
}

data class ManualZoneSummaryCodePrefixRule(var prefix: String) : ManualZoneSummaryRule {
    override fun check(item: ManualZoneSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualZoneSummaryCostRangeRule(var min: Double, var max: Double) : ManualZoneSummaryRule {
    override fun check(item: ManualZoneSummary): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class ManualZoneSummaryLabelLengthRule(var maxLength: Int) : ManualZoneSummaryRule {
    override fun check(item: ManualZoneSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualZoneSummaryCategoryLengthRule(var maxLength: Int) : ManualZoneSummaryRule {
    override fun check(item: ManualZoneSummary): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual zone summary. */
data class ManualZoneSummaryValidator(var rules: MutableList<ManualZoneSummaryRule>) {
    fun validate(item: ManualZoneSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualZoneSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualZoneSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultManualZoneSummaryValidator(): ManualZoneSummaryValidator {
    return ManualZoneSummaryValidator(
        mutableListOf<ManualZoneSummaryRule>(
            ManualZoneSummaryCodePrefixRule("REF"),
            ManualZoneSummaryCostRangeRule(6.3, 2179.6),
            ManualZoneSummaryLabelLengthRule(120),
            ManualZoneSummaryCategoryLengthRule(255),
        ),
    )
}
