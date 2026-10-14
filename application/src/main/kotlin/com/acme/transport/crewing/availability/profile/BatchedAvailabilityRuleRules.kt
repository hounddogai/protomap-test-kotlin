package com.acme.transport.crewing.availability.profile

/** A rule a batched availability rule must pass. Check returns its problems, or none. */
interface BatchedAvailabilityRuleRule {
    fun check(item: BatchedAvailabilityRule): MutableList<String>
}

data class BatchedAvailabilityRuleSummaryLengthRule(var maxLength: Int) : BatchedAvailabilityRuleRule {
    override fun check(item: BatchedAvailabilityRule): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAvailabilityRuleQuantityRangeRule(var min: Int, var max: Int) : BatchedAvailabilityRuleRule {
    override fun check(item: BatchedAvailabilityRule): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAvailabilityRuleRequiredNameRule(var message: String) : BatchedAvailabilityRuleRule {
    override fun check(item: BatchedAvailabilityRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAvailabilityRuleMaximumRangeRule(
    var min: Double,
    var max: Double,
) : BatchedAvailabilityRuleRule {
    override fun check(item: BatchedAvailabilityRule): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class BatchedAvailabilityRuleCodeLengthRule(var maxLength: Int) : BatchedAvailabilityRuleRule {
    override fun check(item: BatchedAvailabilityRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched availability rule. */
data class BatchedAvailabilityRuleValidator(var rules: MutableList<BatchedAvailabilityRuleRule>) {
    fun validate(item: BatchedAvailabilityRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedAvailabilityRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAvailabilityRule>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAvailabilityRuleValidator(): BatchedAvailabilityRuleValidator {
    return BatchedAvailabilityRuleValidator(
        mutableListOf<BatchedAvailabilityRuleRule>(
            BatchedAvailabilityRuleSummaryLengthRule(80),
            BatchedAvailabilityRuleQuantityRangeRule(64, 1000),
            BatchedAvailabilityRuleRequiredNameRule("batched availability rule needs a value"),
            BatchedAvailabilityRuleMaximumRangeRule(7.0, 596.3),
            BatchedAvailabilityRuleCodeLengthRule(16),
        ),
    )
}
