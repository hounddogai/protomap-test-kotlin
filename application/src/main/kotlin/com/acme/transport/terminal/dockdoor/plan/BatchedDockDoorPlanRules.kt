package com.acme.transport.terminal.dockdoor.plan

import com.acme.transport.kit.text.normalize

/** A rule a batched dock door plan must pass. Check returns its problems, or none. */
interface BatchedDockDoorPlanRule {
    fun check(item: BatchedDockDoorPlan): MutableList<String>
}

data class BatchedDockDoorPlanUpdatedAtRangeRule(var min: Int, var max: Int) : BatchedDockDoorPlanRule {
    override fun check(item: BatchedDockDoorPlan): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorPlanRequiredNameRule(var message: String) : BatchedDockDoorPlanRule {
    override fun check(item: BatchedDockDoorPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorPlanAllowedNameRule(var allowed: MutableList<String>) : BatchedDockDoorPlanRule {
    override fun check(item: BatchedDockDoorPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorPlanRequiredTitleRule(var message: String) : BatchedDockDoorPlanRule {
    override fun check(item: BatchedDockDoorPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorPlanCodeLengthRule(var maxLength: Int) : BatchedDockDoorPlanRule {
    override fun check(item: BatchedDockDoorPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched dock door plan. */
data class BatchedDockDoorPlanValidator(var rules: MutableList<BatchedDockDoorPlanRule>) {
    fun validate(item: BatchedDockDoorPlan): MutableList<String> {
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

    fun requireValid(item: BatchedDockDoorPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDockDoorPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDockDoorPlanValidator(): BatchedDockDoorPlanValidator {
    return BatchedDockDoorPlanValidator(
        mutableListOf<BatchedDockDoorPlanRule>(
            BatchedDockDoorPlanUpdatedAtRangeRule(16, 3600),
            BatchedDockDoorPlanRequiredNameRule("batched dock door plan needs an entry"),
            BatchedDockDoorPlanAllowedNameRule(mutableListOf<String>()),
            BatchedDockDoorPlanRequiredTitleRule("batched dock door plan needs an entry"),
            BatchedDockDoorPlanCodeLengthRule(255),
        ),
    )
}
