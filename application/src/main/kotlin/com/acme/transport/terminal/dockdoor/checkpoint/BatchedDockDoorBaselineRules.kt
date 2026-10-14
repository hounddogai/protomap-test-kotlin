package com.acme.transport.terminal.dockdoor.checkpoint

/** A rule a batched dock door baseline must pass. Check returns its problems, or none. */
interface BatchedDockDoorBaselineRule {
    fun check(item: BatchedDockDoorBaseline): MutableList<String>
}

data class BatchedDockDoorBaselineScoreRangeRule(
    var min: Double,
    var max: Double,
) : BatchedDockDoorBaselineRule {
    override fun check(item: BatchedDockDoorBaseline): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorBaselineCodeLengthRule(var maxLength: Int) : BatchedDockDoorBaselineRule {
    override fun check(item: BatchedDockDoorBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorBaselineRequiredCarrierRule(var message: String) : BatchedDockDoorBaselineRule {
    override fun check(item: BatchedDockDoorBaseline): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorBaselineRequiredTitleRule(var message: String) : BatchedDockDoorBaselineRule {
    override fun check(item: BatchedDockDoorBaseline): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorBaselineCapacityRangeRule(var min: Int, var max: Int) : BatchedDockDoorBaselineRule {
    override fun check(item: BatchedDockDoorBaseline): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched dock door baseline. */
data class BatchedDockDoorBaselineValidator(var rules: MutableList<BatchedDockDoorBaselineRule>) {
    fun validate(item: BatchedDockDoorBaseline): MutableList<String> {
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

    fun requireValid(item: BatchedDockDoorBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDockDoorBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDockDoorBaselineValidator(): BatchedDockDoorBaselineValidator {
    return BatchedDockDoorBaselineValidator(
        mutableListOf<BatchedDockDoorBaselineRule>(
            BatchedDockDoorBaselineScoreRangeRule(9.9, 4052.8),
            BatchedDockDoorBaselineCodeLengthRule(64),
            BatchedDockDoorBaselineRequiredCarrierRule("batched dock door baseline needs a value"),
            BatchedDockDoorBaselineRequiredTitleRule("batched dock door baseline needs an entry"),
            BatchedDockDoorBaselineCapacityRangeRule(64, 80),
        ),
    )
}
