package com.acme.transport.fleetops.tire.overrideitem

/** A rule a batched tire tier must pass. Check returns its problems, or none. */
interface BatchedTireTierRule {
    fun check(item: BatchedTireTier): MutableList<String>
}

data class BatchedTireTierPalletsRangeRule(var min: Int, var max: Int) : BatchedTireTierRule {
    override fun check(item: BatchedTireTier): MutableList<String> {
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

data class BatchedTireTierRetriesRangeRule(var min: Int, var max: Int) : BatchedTireTierRule {
    override fun check(item: BatchedTireTier): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class BatchedTireTierRequiredNameRule(var message: String) : BatchedTireTierRule {
    override fun check(item: BatchedTireTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched tire tier. */
data class BatchedTireTierValidator(var rules: MutableList<BatchedTireTierRule>) {
    fun validate(item: BatchedTireTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTireTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTireTier>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTireTierValidator(): BatchedTireTierValidator {
    return BatchedTireTierValidator(
        mutableListOf<BatchedTireTierRule>(
            BatchedTireTierPalletsRangeRule(1000, 3600),
            BatchedTireTierRetriesRangeRule(16, 80),
            BatchedTireTierRequiredNameRule("batched tire tier needs a value"),
        ),
    )
}
