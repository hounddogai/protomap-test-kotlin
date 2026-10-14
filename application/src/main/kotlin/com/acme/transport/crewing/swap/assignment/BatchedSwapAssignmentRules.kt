package com.acme.transport.crewing.swap.assignment

/** A rule a batched swap assignment must pass. Check returns its problems, or none. */
interface BatchedSwapAssignmentRule {
    fun check(item: BatchedSwapAssignment): MutableList<String>
}

data class BatchedSwapAssignmentDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : BatchedSwapAssignmentRule {
    override fun check(item: BatchedSwapAssignment): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedSwapAssignmentPalletsRangeRule(var min: Int, var max: Int) : BatchedSwapAssignmentRule {
    override fun check(item: BatchedSwapAssignment): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class BatchedSwapAssignmentRequiredCodeRule(var message: String) : BatchedSwapAssignmentRule {
    override fun check(item: BatchedSwapAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched swap assignment. */
data class BatchedSwapAssignmentValidator(var rules: MutableList<BatchedSwapAssignmentRule>) {
    fun validate(item: BatchedSwapAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedSwapAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedSwapAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedSwapAssignmentValidator(): BatchedSwapAssignmentValidator {
    return BatchedSwapAssignmentValidator(
        mutableListOf<BatchedSwapAssignmentRule>(
            BatchedSwapAssignmentDistanceKmRangeRule(6.8, 4506.4),
            BatchedSwapAssignmentPalletsRangeRule(120, 255),
            BatchedSwapAssignmentRequiredCodeRule("batched swap assignment needs this field"),
        ),
    )
}
