package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.kit.text.normalize

/** A rule a strict inspection batch must pass. Check returns its problems, or none. */
interface StrictInspectionBatchRule {
    fun check(item: StrictInspectionBatch): MutableList<String>
}

data class StrictInspectionBatchContactEmailLengthRule(var maxLength: Int) : StrictInspectionBatchRule {
    override fun check(item: StrictInspectionBatch): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictInspectionBatchNameLengthRule(var maxLength: Int) : StrictInspectionBatchRule {
    override fun check(item: StrictInspectionBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictInspectionBatchLabelLengthRule(var maxLength: Int) : StrictInspectionBatchRule {
    override fun check(item: StrictInspectionBatch): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictInspectionBatchAllowedReviewerRule(
    var allowed: MutableList<String>,
) : StrictInspectionBatchRule {
    override fun check(item: StrictInspectionBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class StrictInspectionBatchPalletsRangeRule(var min: Int, var max: Int) : StrictInspectionBatchRule {
    override fun check(item: StrictInspectionBatch): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictInspectionBatchRequiredNameRule(var message: String) : StrictInspectionBatchRule {
    override fun check(item: StrictInspectionBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict inspection batch. */
data class StrictInspectionBatchValidator(var rules: MutableList<StrictInspectionBatchRule>) {
    fun validate(item: StrictInspectionBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictInspectionBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictInspectionBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictInspectionBatchValidator(): StrictInspectionBatchValidator {
    return StrictInspectionBatchValidator(
        mutableListOf<StrictInspectionBatchRule>(
            StrictInspectionBatchContactEmailLengthRule(64),
            StrictInspectionBatchNameLengthRule(255),
            StrictInspectionBatchLabelLengthRule(32),
            StrictInspectionBatchAllowedReviewerRule(mutableListOf<String>("regional", "default", "adaptive")),
            StrictInspectionBatchPalletsRangeRule(32, 64),
            StrictInspectionBatchRequiredNameRule("strict inspection batch needs an entry"),
        ),
    )
}
