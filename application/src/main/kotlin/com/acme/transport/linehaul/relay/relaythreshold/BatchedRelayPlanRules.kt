package com.acme.transport.linehaul.relay.relaythreshold

/** A rule a batched relay plan must pass. Check returns its problems, or none. */
interface BatchedRelayPlanRule {
    fun check(item: BatchedRelayPlan): MutableList<String>
}

data class BatchedRelayPlanShiftCodeLengthRule(var maxLength: Int) : BatchedRelayPlanRule {
    override fun check(item: BatchedRelayPlan): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRelayPlanLabelLengthRule(var maxLength: Int) : BatchedRelayPlanRule {
    override fun check(item: BatchedRelayPlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRelayPlanLimitRangeRule(var min: Int, var max: Int) : BatchedRelayPlanRule {
    override fun check(item: BatchedRelayPlan): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched relay plan. */
data class BatchedRelayPlanValidator(var rules: MutableList<BatchedRelayPlanRule>) {
    fun validate(item: BatchedRelayPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedRelayPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRelayPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRelayPlanValidator(): BatchedRelayPlanValidator {
    return BatchedRelayPlanValidator(
        mutableListOf<BatchedRelayPlanRule>(
            BatchedRelayPlanShiftCodeLengthRule(1000),
            BatchedRelayPlanLabelLengthRule(80),
            BatchedRelayPlanLimitRangeRule(32, 64),
        ),
    )
}
