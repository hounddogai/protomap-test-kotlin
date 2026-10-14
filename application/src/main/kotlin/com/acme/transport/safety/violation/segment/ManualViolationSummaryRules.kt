package com.acme.transport.safety.violation.segment

/** A rule a manual violation summary must pass. Check returns its problems, or none. */
interface ManualViolationSummaryRule {
    fun check(item: ManualViolationSummary): MutableList<String>
}

data class ManualViolationSummaryNameLengthRule(var maxLength: Int) : ManualViolationSummaryRule {
    override fun check(item: ManualViolationSummary): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualViolationSummaryLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : ManualViolationSummaryRule {
    override fun check(item: ManualViolationSummary): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class ManualViolationSummaryLocaleLengthRule(var maxLength: Int) : ManualViolationSummaryRule {
    override fun check(item: ManualViolationSummary): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualViolationSummaryThresholdRangeRule(
    var min: Double,
    var max: Double,
) : ManualViolationSummaryRule {
    override fun check(item: ManualViolationSummary): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual violation summary. */
data class ManualViolationSummaryValidator(var rules: MutableList<ManualViolationSummaryRule>) {
    fun validate(item: ManualViolationSummary): MutableList<String> {
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

    fun requireValid(item: ManualViolationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualViolationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultManualViolationSummaryValidator(): ManualViolationSummaryValidator {
    return ManualViolationSummaryValidator(
        mutableListOf<ManualViolationSummaryRule>(
            ManualViolationSummaryNameLengthRule(120),
            ManualViolationSummaryLoadKgRangeRule(4.0, 4901.0),
            ManualViolationSummaryLocaleLengthRule(16),
            ManualViolationSummaryThresholdRangeRule(6.3, 3883.8),
        ),
    )
}
