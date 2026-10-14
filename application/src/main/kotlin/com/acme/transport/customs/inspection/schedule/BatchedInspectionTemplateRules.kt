package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.text.normalize

/** A rule a batched inspection template must pass. Check returns its problems, or none. */
interface BatchedInspectionTemplateRule {
    fun check(item: BatchedInspectionTemplate): MutableList<String>
}

data class BatchedInspectionTemplateMarginRangeRule(
    var min: Double,
    var max: Double,
) : BatchedInspectionTemplateRule {
    override fun check(item: BatchedInspectionTemplate): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class BatchedInspectionTemplateExternalRefLengthRule(var maxLength: Int) : BatchedInspectionTemplateRule {
    override fun check(item: BatchedInspectionTemplate): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionTemplateDriverIdLengthRule(var maxLength: Int) : BatchedInspectionTemplateRule {
    override fun check(item: BatchedInspectionTemplate): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionTemplateAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : BatchedInspectionTemplateRule {
    override fun check(item: BatchedInspectionTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched inspection template. */
data class BatchedInspectionTemplateValidator(var rules: MutableList<BatchedInspectionTemplateRule>) {
    fun validate(item: BatchedInspectionTemplate): MutableList<String> {
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

    fun requireValid(item: BatchedInspectionTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedInspectionTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedInspectionTemplateValidator(): BatchedInspectionTemplateValidator {
    return BatchedInspectionTemplateValidator(
        mutableListOf<BatchedInspectionTemplateRule>(
            BatchedInspectionTemplateMarginRangeRule(0.6, 4153.5),
            BatchedInspectionTemplateExternalRefLengthRule(80),
            BatchedInspectionTemplateDriverIdLengthRule(255),
            BatchedInspectionTemplateAllowedExternalRefRule(mutableListOf<String>("scheduled", "batched")),
        ),
    )
}
