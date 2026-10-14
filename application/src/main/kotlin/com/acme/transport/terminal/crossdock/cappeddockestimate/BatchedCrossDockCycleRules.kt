package com.acme.transport.terminal.crossdock.cappeddockestimate

/** A rule a batched cross dock cycle must pass. Check returns its problems, or none. */
interface BatchedCrossDockCycleRule {
    fun check(item: BatchedCrossDockCycle): MutableList<String>
}

data class BatchedCrossDockCycleAttemptsRangeRule(var min: Int, var max: Int) : BatchedCrossDockCycleRule {
    override fun check(item: BatchedCrossDockCycle): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class BatchedCrossDockCycleThresholdRangeRule(
    var min: Double,
    var max: Double,
) : BatchedCrossDockCycleRule {
    override fun check(item: BatchedCrossDockCycle): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockCycleRequiredLabelRule(var message: String) : BatchedCrossDockCycleRule {
    override fun check(item: BatchedCrossDockCycle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockCycleRequiredRegionRule(var message: String) : BatchedCrossDockCycleRule {
    override fun check(item: BatchedCrossDockCycle): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockCycleCodePrefixRule(var prefix: String) : BatchedCrossDockCycleRule {
    override fun check(item: BatchedCrossDockCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched cross dock cycle. */
data class BatchedCrossDockCycleValidator(var rules: MutableList<BatchedCrossDockCycleRule>) {
    fun validate(item: BatchedCrossDockCycle): MutableList<String> {
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

    fun requireValid(item: BatchedCrossDockCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCrossDockCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCrossDockCycleValidator(): BatchedCrossDockCycleValidator {
    return BatchedCrossDockCycleValidator(
        mutableListOf<BatchedCrossDockCycleRule>(
            BatchedCrossDockCycleAttemptsRangeRule(120, 86400),
            BatchedCrossDockCycleThresholdRangeRule(6.5, 3864.1),
            BatchedCrossDockCycleRequiredLabelRule("batched cross dock cycle needs this field"),
            BatchedCrossDockCycleRequiredRegionRule("batched cross dock cycle needs a value"),
            BatchedCrossDockCycleCodePrefixRule("ACM"),
        ),
    )
}
