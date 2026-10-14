package com.acme.transport.customs.broker.schedule

/** A rule a weighted broker plan must pass. Check returns its problems, or none. */
interface WeightedBrokerPlanRule {
    fun check(item: WeightedBrokerPlan): MutableList<String>
}

data class WeightedBrokerPlanQuantityRangeRule(var min: Int, var max: Int) : WeightedBrokerPlanRule {
    override fun check(item: WeightedBrokerPlan): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBrokerPlanBudgetRangeRule(var min: Double, var max: Double) : WeightedBrokerPlanRule {
    override fun check(item: WeightedBrokerPlan): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBrokerPlanChecksumLengthRule(var maxLength: Int) : WeightedBrokerPlanRule {
    override fun check(item: WeightedBrokerPlan): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted broker plan. */
data class WeightedBrokerPlanValidator(var rules: MutableList<WeightedBrokerPlanRule>) {
    fun validate(item: WeightedBrokerPlan): MutableList<String> {
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

    fun requireValid(item: WeightedBrokerPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedBrokerPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedBrokerPlanValidator(): WeightedBrokerPlanValidator {
    return WeightedBrokerPlanValidator(
        mutableListOf<WeightedBrokerPlanRule>(
            WeightedBrokerPlanQuantityRangeRule(80, 120),
            WeightedBrokerPlanBudgetRangeRule(2.4, 1343.8),
            WeightedBrokerPlanChecksumLengthRule(16),
        ),
    )
}
