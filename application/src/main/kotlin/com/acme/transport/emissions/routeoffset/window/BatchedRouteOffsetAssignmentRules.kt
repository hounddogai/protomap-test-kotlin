package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.kit.text.normalize

/** A rule a batched route offset assignment must pass. Check returns its problems, or none. */
interface BatchedRouteOffsetAssignmentRule {
    fun check(item: BatchedRouteOffsetAssignment): MutableList<String>
}

data class BatchedRouteOffsetAssignmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : BatchedRouteOffsetAssignmentRule {
    override fun check(item: BatchedRouteOffsetAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRouteOffsetAssignmentCapacityRangeRule(
    var min: Int,
    var max: Int,
) : BatchedRouteOffsetAssignmentRule {
    override fun check(item: BatchedRouteOffsetAssignment): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRouteOffsetAssignmentRequiredCodeRule(
    var message: String,
) : BatchedRouteOffsetAssignmentRule {
    override fun check(item: BatchedRouteOffsetAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRouteOffsetAssignmentCodeLengthRule(var maxLength: Int) : BatchedRouteOffsetAssignmentRule {
    override fun check(item: BatchedRouteOffsetAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched route offset assignment. */
data class BatchedRouteOffsetAssignmentValidator(var rules: MutableList<BatchedRouteOffsetAssignmentRule>) {
    fun validate(item: BatchedRouteOffsetAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedRouteOffsetAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRouteOffsetAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRouteOffsetAssignmentValidator(): BatchedRouteOffsetAssignmentValidator {
    return BatchedRouteOffsetAssignmentValidator(
        mutableListOf<BatchedRouteOffsetAssignmentRule>(
            BatchedRouteOffsetAssignmentAllowedCodeRule(mutableListOf<String>()),
            BatchedRouteOffsetAssignmentCapacityRangeRule(120, 1000),
            BatchedRouteOffsetAssignmentRequiredCodeRule("batched route offset assignment needs a value"),
            BatchedRouteOffsetAssignmentCodeLengthRule(80),
        ),
    )
}
