package com.acme.transport.crewing.qualification.summary

/** A rule a batched qualification summary must pass. Check returns its problems, or none. */
interface BatchedQualificationSummaryRule {
    fun check(item: BatchedQualificationSummary): MutableList<String>
}

data class BatchedQualificationSummaryCodePrefixRule(var prefix: String) : BatchedQualificationSummaryRule {
    override fun check(item: BatchedQualificationSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationSummaryLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : BatchedQualificationSummaryRule {
    override fun check(item: BatchedQualificationSummary): MutableList<String> {
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

data class BatchedQualificationSummaryNameLengthRule(var maxLength: Int) : BatchedQualificationSummaryRule {
    override fun check(item: BatchedQualificationSummary): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationSummaryLabelLengthRule(var maxLength: Int) : BatchedQualificationSummaryRule {
    override fun check(item: BatchedQualificationSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched qualification summary. */
data class BatchedQualificationSummaryValidator(var rules: MutableList<BatchedQualificationSummaryRule>) {
    fun validate(item: BatchedQualificationSummary): MutableList<String> {
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

    fun requireValid(item: BatchedQualificationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedQualificationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedQualificationSummaryValidator(): BatchedQualificationSummaryValidator {
    return BatchedQualificationSummaryValidator(
        mutableListOf<BatchedQualificationSummaryRule>(
            BatchedQualificationSummaryCodePrefixRule("X"),
            BatchedQualificationSummaryLoadKgRangeRule(7.4, 4979.4),
            BatchedQualificationSummaryNameLengthRule(80),
            BatchedQualificationSummaryLabelLengthRule(32),
        ),
    )
}
