package com.acme.transport.customs.inspection.batch

/** A rule a inspection batch must pass. Check returns its problems, or none. */
interface InspectionBatchRule {
    fun check(item: InspectionBatch): MutableList<String>
}

data class InspectionBatchLabelLengthRule(var maxLength: Int) : InspectionBatchRule {
    override fun check(item: InspectionBatch): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionBatchRequiredCodeRule(var message: String) : InspectionBatchRule {
    override fun check(item: InspectionBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionBatchEndsAtRangeRule(var min: Int, var max: Int) : InspectionBatchRule {
    override fun check(item: InspectionBatch): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a inspection batch. */
data class InspectionBatchValidator(var rules: MutableList<InspectionBatchRule>) {
    fun validate(item: InspectionBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionBatchValidator(): InspectionBatchValidator {
    return InspectionBatchValidator(
        mutableListOf<InspectionBatchRule>(
            InspectionBatchLabelLengthRule(16),
            InspectionBatchRequiredCodeRule("inspection batch needs an entry"),
            InspectionBatchEndsAtRangeRule(64, 64),
        ),
    )
}
