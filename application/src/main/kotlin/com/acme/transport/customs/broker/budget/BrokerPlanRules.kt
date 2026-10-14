package com.acme.transport.customs.broker.budget

import com.acme.transport.kit.text.normalize

/** A rule a broker plan must pass. Check returns its problems, or none. */
interface BrokerPlanRule {
    fun check(item: BrokerPlan): MutableList<String>
}

data class BrokerPlanAllowedTitleRule(var allowed: MutableList<String>) : BrokerPlanRule {
    override fun check(item: BrokerPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class BrokerPlanOwnerIdLengthRule(var maxLength: Int) : BrokerPlanRule {
    override fun check(item: BrokerPlan): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerPlanPalletsRangeRule(var min: Int, var max: Int) : BrokerPlanRule {
    override fun check(item: BrokerPlan): MutableList<String> {
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

data class BrokerPlanBatchSizeRangeRule(var min: Int, var max: Int) : BrokerPlanRule {
    override fun check(item: BrokerPlan): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker plan. */
data class BrokerPlanValidator(var rules: MutableList<BrokerPlanRule>) {
    fun validate(item: BrokerPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerPlanValidator(): BrokerPlanValidator {
    return BrokerPlanValidator(
        mutableListOf<BrokerPlanRule>(
            BrokerPlanAllowedTitleRule(mutableListOf<String>("adaptive")),
            BrokerPlanOwnerIdLengthRule(255),
            BrokerPlanPalletsRangeRule(255, 1000),
            BrokerPlanBatchSizeRangeRule(32, 3600),
        ),
    )
}
