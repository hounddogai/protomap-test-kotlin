package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.text.normalize

/** A rule a batched service area exception must pass. Check returns its problems, or none. */
interface BatchedServiceAreaExceptionRule {
    fun check(item: BatchedServiceAreaException): MutableList<String>
}

data class BatchedServiceAreaExceptionBudgetRangeRule(
    var min: Double,
    var max: Double,
) : BatchedServiceAreaExceptionRule {
    override fun check(item: BatchedServiceAreaException): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServiceAreaExceptionAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : BatchedServiceAreaExceptionRule {
    override fun check(item: BatchedServiceAreaException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is stale")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServiceAreaExceptionRequiredCodeRule(var message: String) : BatchedServiceAreaExceptionRule {
    override fun check(item: BatchedServiceAreaException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedServiceAreaExceptionRequiredLocaleRule(
    var message: String,
) : BatchedServiceAreaExceptionRule {
    override fun check(item: BatchedServiceAreaException): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched service area exception. */
data class BatchedServiceAreaExceptionValidator(var rules: MutableList<BatchedServiceAreaExceptionRule>) {
    fun validate(item: BatchedServiceAreaException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedServiceAreaException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedServiceAreaException>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedServiceAreaExceptionValidator(): BatchedServiceAreaExceptionValidator {
    return BatchedServiceAreaExceptionValidator(
        mutableListOf<BatchedServiceAreaExceptionRule>(
            BatchedServiceAreaExceptionBudgetRangeRule(0.4, 2752.3),
            BatchedServiceAreaExceptionAllowedOwnerIdRule(mutableListOf<String>("manual")),
            BatchedServiceAreaExceptionRequiredCodeRule("batched service area exception needs an entry"),
            BatchedServiceAreaExceptionRequiredLocaleRule("batched service area exception needs an entry"),
        ),
    )
}
