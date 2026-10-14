package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.text.normalize

/** A rule a batched service plan group must pass. Check returns its problems, or none. */
interface BatchedServicePlanGroupRule {
    fun check(item: BatchedServicePlanGroup): MutableList<String>
}

data class BatchedServicePlanGroupNameLengthRule(var maxLength: Int) : BatchedServicePlanGroupRule {
    override fun check(item: BatchedServicePlanGroup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServicePlanGroupCodeLengthRule(var maxLength: Int) : BatchedServicePlanGroupRule {
    override fun check(item: BatchedServicePlanGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServicePlanGroupAllowedNameRule(
    var allowed: MutableList<String>,
) : BatchedServicePlanGroupRule {
    override fun check(item: BatchedServicePlanGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServicePlanGroupRequiredCodeRule(var message: String) : BatchedServicePlanGroupRule {
    override fun check(item: BatchedServicePlanGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedServicePlanGroupRequiredNameRule(var message: String) : BatchedServicePlanGroupRule {
    override fun check(item: BatchedServicePlanGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedServicePlanGroupRateRangeRule(
    var min: Double,
    var max: Double,
) : BatchedServicePlanGroupRule {
    override fun check(item: BatchedServicePlanGroup): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched service plan group. */
data class BatchedServicePlanGroupValidator(var rules: MutableList<BatchedServicePlanGroupRule>) {
    fun validate(item: BatchedServicePlanGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedServicePlanGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedServicePlanGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedServicePlanGroupValidator(): BatchedServicePlanGroupValidator {
    return BatchedServicePlanGroupValidator(
        mutableListOf<BatchedServicePlanGroupRule>(
            BatchedServicePlanGroupNameLengthRule(64),
            BatchedServicePlanGroupCodeLengthRule(120),
            BatchedServicePlanGroupAllowedNameRule(mutableListOf<String>()),
            BatchedServicePlanGroupRequiredCodeRule("batched service plan group needs a value"),
            BatchedServicePlanGroupRequiredNameRule("batched service plan group needs a value"),
            BatchedServicePlanGroupRateRangeRule(2.3, 1190.4),
        ),
    )
}
