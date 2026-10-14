package com.acme.transport.network.hub.batch

/** A rule a strict hub batch must pass. Check returns its problems, or none. */
interface StrictHubBatchRule {
    fun check(item: StrictHubBatch): MutableList<String>
}

data class StrictHubBatchSequenceRangeRule(var min: Int, var max: Int) : StrictHubBatchRule {
    override fun check(item: StrictHubBatch): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class StrictHubBatchCodeLengthRule(var maxLength: Int) : StrictHubBatchRule {
    override fun check(item: StrictHubBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictHubBatchDescriptionLengthRule(var maxLength: Int) : StrictHubBatchRule {
    override fun check(item: StrictHubBatch): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict hub batch. */
data class StrictHubBatchValidator(var rules: MutableList<StrictHubBatchRule>) {
    fun validate(item: StrictHubBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictHubBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictHubBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictHubBatchValidator(): StrictHubBatchValidator {
    return StrictHubBatchValidator(
        mutableListOf<StrictHubBatchRule>(
            StrictHubBatchSequenceRangeRule(64, 80),
            StrictHubBatchCodeLengthRule(80),
            StrictHubBatchDescriptionLengthRule(64),
        ),
    )
}
