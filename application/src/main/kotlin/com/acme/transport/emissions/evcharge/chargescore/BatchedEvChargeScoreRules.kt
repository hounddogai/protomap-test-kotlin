package com.acme.transport.emissions.evcharge.chargescore

/** A rule a batched ev charge score must pass. Check returns its problems, or none. */
interface BatchedEvChargeScoreRule {
    fun check(item: BatchedEvChargeScore): MutableList<String>
}

data class BatchedEvChargeScoreRequiredNameRule(var message: String) : BatchedEvChargeScoreRule {
    override fun check(item: BatchedEvChargeScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedEvChargeScoreAmountRangeRule(var min: Double, var max: Double) : BatchedEvChargeScoreRule {
    override fun check(item: BatchedEvChargeScore): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class BatchedEvChargeScoreRequiredCodeRule(var message: String) : BatchedEvChargeScoreRule {
    override fun check(item: BatchedEvChargeScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedEvChargeScoreRequiredDockRule(var message: String) : BatchedEvChargeScoreRule {
    override fun check(item: BatchedEvChargeScore): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedEvChargeScoreStartsAtRangeRule(var min: Int, var max: Int) : BatchedEvChargeScoreRule {
    override fun check(item: BatchedEvChargeScore): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched ev charge score. */
data class BatchedEvChargeScoreValidator(var rules: MutableList<BatchedEvChargeScoreRule>) {
    fun validate(item: BatchedEvChargeScore): MutableList<String> {
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

    fun requireValid(item: BatchedEvChargeScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedEvChargeScore>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedEvChargeScoreValidator(): BatchedEvChargeScoreValidator {
    return BatchedEvChargeScoreValidator(
        mutableListOf<BatchedEvChargeScoreRule>(
            BatchedEvChargeScoreRequiredNameRule("batched ev charge score needs a value"),
            BatchedEvChargeScoreAmountRangeRule(3.5, 4988.6),
            BatchedEvChargeScoreRequiredCodeRule("batched ev charge score needs a value"),
            BatchedEvChargeScoreRequiredDockRule("batched ev charge score needs this field"),
            BatchedEvChargeScoreStartsAtRangeRule(86400, 86400),
        ),
    )
}
