package com.acme.transport.network.transittime.draft

/** A rule a batched transit time rule must pass. Check returns its problems, or none. */
interface BatchedTransitTimeRuleRule {
    fun check(item: BatchedTransitTimeRule): MutableList<String>
}

data class BatchedTransitTimeRuleStartsAtRangeRule(var min: Int, var max: Int) : BatchedTransitTimeRuleRule {
    override fun check(item: BatchedTransitTimeRule): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTransitTimeRuleCodeLengthRule(var maxLength: Int) : BatchedTransitTimeRuleRule {
    override fun check(item: BatchedTransitTimeRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTransitTimeRuleRequiredCodeRule(var message: String) : BatchedTransitTimeRuleRule {
    override fun check(item: BatchedTransitTimeRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched transit time rule. */
data class BatchedTransitTimeRuleValidator(var rules: MutableList<BatchedTransitTimeRuleRule>) {
    fun validate(item: BatchedTransitTimeRule): MutableList<String> {
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

    fun requireValid(item: BatchedTransitTimeRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTransitTimeRule>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTransitTimeRuleValidator(): BatchedTransitTimeRuleValidator {
    return BatchedTransitTimeRuleValidator(
        mutableListOf<BatchedTransitTimeRuleRule>(
            BatchedTransitTimeRuleStartsAtRangeRule(120, 1000),
            BatchedTransitTimeRuleCodeLengthRule(80),
            BatchedTransitTimeRuleRequiredCodeRule("batched transit time rule needs a value"),
        ),
    )
}
