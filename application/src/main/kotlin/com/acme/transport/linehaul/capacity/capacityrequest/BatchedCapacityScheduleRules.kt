package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.text.normalize

/** A rule a batched capacity schedule must pass. Check returns its problems, or none. */
interface BatchedCapacityScheduleRule {
    fun check(item: BatchedCapacitySchedule): MutableList<String>
}

data class BatchedCapacityScheduleAllowedCodeRule(
    var allowed: MutableList<String>,
) : BatchedCapacityScheduleRule {
    override fun check(item: BatchedCapacitySchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityScheduleRequiredCodeRule(var message: String) : BatchedCapacityScheduleRule {
    override fun check(item: BatchedCapacitySchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityScheduleRequiredTitleRule(var message: String) : BatchedCapacityScheduleRule {
    override fun check(item: BatchedCapacitySchedule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityScheduleRateRangeRule(
    var min: Double,
    var max: Double,
) : BatchedCapacityScheduleRule {
    override fun check(item: BatchedCapacitySchedule): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched capacity schedule. */
data class BatchedCapacityScheduleValidator(var rules: MutableList<BatchedCapacityScheduleRule>) {
    fun validate(item: BatchedCapacitySchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedCapacitySchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCapacitySchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCapacityScheduleValidator(): BatchedCapacityScheduleValidator {
    return BatchedCapacityScheduleValidator(
        mutableListOf<BatchedCapacityScheduleRule>(
            BatchedCapacityScheduleAllowedCodeRule(mutableListOf<String>("rolling")),
            BatchedCapacityScheduleRequiredCodeRule("batched capacity schedule needs an entry"),
            BatchedCapacityScheduleRequiredTitleRule("batched capacity schedule needs a value"),
            BatchedCapacityScheduleRateRangeRule(3.5, 1928.2),
        ),
    )
}
