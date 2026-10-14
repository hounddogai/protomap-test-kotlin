package com.acme.transport.network.hub.legacyhubplan

/** A rule a batched hub exception must pass. Check returns its problems, or none. */
interface BatchedHubExceptionRule {
    fun check(item: BatchedHubException): MutableList<String>
}

data class BatchedHubExceptionCostRangeRule(var min: Double, var max: Double) : BatchedHubExceptionRule {
    override fun check(item: BatchedHubException): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class BatchedHubExceptionDescriptionLengthRule(var maxLength: Int) : BatchedHubExceptionRule {
    override fun check(item: BatchedHubException): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedHubExceptionCodeLengthRule(var maxLength: Int) : BatchedHubExceptionRule {
    override fun check(item: BatchedHubException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched hub exception. */
data class BatchedHubExceptionValidator(var rules: MutableList<BatchedHubExceptionRule>) {
    fun validate(item: BatchedHubException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedHubException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedHubException>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedHubExceptionValidator(): BatchedHubExceptionValidator {
    return BatchedHubExceptionValidator(
        mutableListOf<BatchedHubExceptionRule>(
            BatchedHubExceptionCostRangeRule(5.2, 3737.2),
            BatchedHubExceptionDescriptionLengthRule(120),
            BatchedHubExceptionCodeLengthRule(86400),
        ),
    )
}
