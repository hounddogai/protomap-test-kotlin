package com.acme.transport.terminal.sortation.window

/** A rule a pending sortation batch must pass. Check returns its problems, or none. */
interface PendingSortationBatchRule {
    fun check(item: PendingSortationBatch): MutableList<String>
}

data class PendingSortationBatchPriorityRangeRule(var min: Int, var max: Int) : PendingSortationBatchRule {
    override fun check(item: PendingSortationBatch): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class PendingSortationBatchRegionLengthRule(var maxLength: Int) : PendingSortationBatchRule {
    override fun check(item: PendingSortationBatch): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingSortationBatchCostRangeRule(var min: Double, var max: Double) : PendingSortationBatchRule {
    override fun check(item: PendingSortationBatch): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending sortation batch. */
data class PendingSortationBatchValidator(var rules: MutableList<PendingSortationBatchRule>) {
    fun validate(item: PendingSortationBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingSortationBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingSortationBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingSortationBatchValidator(): PendingSortationBatchValidator {
    return PendingSortationBatchValidator(
        mutableListOf<PendingSortationBatchRule>(
            PendingSortationBatchPriorityRangeRule(80, 1000),
            PendingSortationBatchRegionLengthRule(1000),
            PendingSortationBatchCostRangeRule(3.0, 2624.9),
        ),
    )
}
