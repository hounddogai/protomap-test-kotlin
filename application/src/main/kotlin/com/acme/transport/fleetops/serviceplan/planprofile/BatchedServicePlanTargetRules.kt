package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.kit.text.normalize

/** A rule a batched service plan target must pass. Check returns its problems, or none. */
interface BatchedServicePlanTargetRule {
    fun check(item: BatchedServicePlanTarget): MutableList<String>
}

data class BatchedServicePlanTargetAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : BatchedServicePlanTargetRule {
    override fun check(item: BatchedServicePlanTarget): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServicePlanTargetRequiredCodeRule(var message: String) : BatchedServicePlanTargetRule {
    override fun check(item: BatchedServicePlanTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedServicePlanTargetAllowedNameRule(
    var allowed: MutableList<String>,
) : BatchedServicePlanTargetRule {
    override fun check(item: BatchedServicePlanTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServicePlanTargetCodeLengthRule(var maxLength: Int) : BatchedServicePlanTargetRule {
    override fun check(item: BatchedServicePlanTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched service plan target. */
data class BatchedServicePlanTargetValidator(var rules: MutableList<BatchedServicePlanTargetRule>) {
    fun validate(item: BatchedServicePlanTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedServicePlanTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedServicePlanTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedServicePlanTargetValidator(): BatchedServicePlanTargetValidator {
    return BatchedServicePlanTargetValidator(
        mutableListOf<BatchedServicePlanTargetRule>(
            BatchedServicePlanTargetAttemptsRangeRule(80, 86400),
            BatchedServicePlanTargetRequiredCodeRule("batched service plan target needs a value"),
            BatchedServicePlanTargetAllowedNameRule(mutableListOf<String>("pending", "rolling")),
            BatchedServicePlanTargetCodeLengthRule(120),
        ),
    )
}
