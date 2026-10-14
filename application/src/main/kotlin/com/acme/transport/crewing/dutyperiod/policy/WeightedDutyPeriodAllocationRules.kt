package com.acme.transport.crewing.dutyperiod.policy

/** A rule a weighted duty period allocation must pass. Check returns its problems, or none. */
interface WeightedDutyPeriodAllocationRule {
    fun check(item: WeightedDutyPeriodAllocation): MutableList<String>
}

data class WeightedDutyPeriodAllocationRequiredCarrierRule(
    var message: String,
) : WeightedDutyPeriodAllocationRule {
    override fun check(item: WeightedDutyPeriodAllocation): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDutyPeriodAllocationPriorityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedDutyPeriodAllocationRule {
    override fun check(item: WeightedDutyPeriodAllocation): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class WeightedDutyPeriodAllocationCarrierLengthRule(
    var maxLength: Int,
) : WeightedDutyPeriodAllocationRule {
    override fun check(item: WeightedDutyPeriodAllocation): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted duty period allocation. */
data class WeightedDutyPeriodAllocationValidator(var rules: MutableList<WeightedDutyPeriodAllocationRule>) {
    fun validate(item: WeightedDutyPeriodAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedDutyPeriodAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDutyPeriodAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDutyPeriodAllocationValidator(): WeightedDutyPeriodAllocationValidator {
    return WeightedDutyPeriodAllocationValidator(
        mutableListOf<WeightedDutyPeriodAllocationRule>(
            WeightedDutyPeriodAllocationRequiredCarrierRule("weighted duty period allocation needs an entry"),
            WeightedDutyPeriodAllocationPriorityRangeRule(1000, 1000),
            WeightedDutyPeriodAllocationCarrierLengthRule(16),
        ),
    )
}
