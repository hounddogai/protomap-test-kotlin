package com.acme.transport.crewing.qualification.schedule

/** A rule a batched qualification milestone must pass. Check returns its problems, or none. */
interface BatchedQualificationMilestoneRule {
    fun check(item: BatchedQualificationMilestone): MutableList<String>
}

data class BatchedQualificationMilestoneCodeLengthRule(var maxLength: Int) : BatchedQualificationMilestoneRule {
    override fun check(item: BatchedQualificationMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationMilestoneFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : BatchedQualificationMilestoneRule {
    override fun check(item: BatchedQualificationMilestone): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationMilestoneDiscountRangeRule(
    var min: Double,
    var max: Double,
) : BatchedQualificationMilestoneRule {
    override fun check(item: BatchedQualificationMilestone): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched qualification milestone. */
data class BatchedQualificationMilestoneValidator(var rules: MutableList<BatchedQualificationMilestoneRule>) {
    fun validate(item: BatchedQualificationMilestone): MutableList<String> {
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

    fun requireValid(item: BatchedQualificationMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedQualificationMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedQualificationMilestoneValidator(): BatchedQualificationMilestoneValidator {
    return BatchedQualificationMilestoneValidator(
        mutableListOf<BatchedQualificationMilestoneRule>(
            BatchedQualificationMilestoneCodeLengthRule(32),
            BatchedQualificationMilestoneFuelLitersRangeRule(8.4, 4403.0),
            BatchedQualificationMilestoneDiscountRangeRule(0.5, 4206.6),
        ),
    )
}
