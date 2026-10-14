package com.acme.transport.fleetops.defect.budget

/** A rule a strict defect batch must pass. Check returns its problems, or none. */
interface StrictDefectBatchRule {
    fun check(item: StrictDefectBatch): MutableList<String>
}

data class StrictDefectBatchRequiredNameRule(var message: String) : StrictDefectBatchRule {
    override fun check(item: StrictDefectBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDefectBatchActiveGuardRule(var limit: Int) : StrictDefectBatchRule {
    override fun check(item: StrictDefectBatch): MutableList<String> {
        if (item.active && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class StrictDefectBatchThresholdRangeRule(var min: Double, var max: Double) : StrictDefectBatchRule {
    override fun check(item: StrictDefectBatch): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictDefectBatchDiscountRangeRule(var min: Double, var max: Double) : StrictDefectBatchRule {
    override fun check(item: StrictDefectBatch): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a strict defect batch. */
data class StrictDefectBatchValidator(var rules: MutableList<StrictDefectBatchRule>) {
    fun validate(item: StrictDefectBatch): MutableList<String> {
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

    fun requireValid(item: StrictDefectBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDefectBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDefectBatchValidator(): StrictDefectBatchValidator {
    return StrictDefectBatchValidator(
        mutableListOf<StrictDefectBatchRule>(
            StrictDefectBatchRequiredNameRule("strict defect batch needs a value"),
            StrictDefectBatchActiveGuardRule(255),
            StrictDefectBatchThresholdRangeRule(8.0, 759.8),
            StrictDefectBatchDiscountRangeRule(6.3, 76.6),
        ),
    )
}
