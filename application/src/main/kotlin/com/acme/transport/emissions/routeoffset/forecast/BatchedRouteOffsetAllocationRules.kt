package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.text.normalize

/** A rule a batched route offset allocation must pass. Check returns its problems, or none. */
interface BatchedRouteOffsetAllocationRule {
    fun check(item: BatchedRouteOffsetAllocation): MutableList<String>
}

data class BatchedRouteOffsetAllocationTitleLengthRule(var maxLength: Int) : BatchedRouteOffsetAllocationRule {
    override fun check(item: BatchedRouteOffsetAllocation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRouteOffsetAllocationMaximumRangeRule(
    var min: Double,
    var max: Double,
) : BatchedRouteOffsetAllocationRule {
    override fun check(item: BatchedRouteOffsetAllocation): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class BatchedRouteOffsetAllocationFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : BatchedRouteOffsetAllocationRule {
    override fun check(item: BatchedRouteOffsetAllocation): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class BatchedRouteOffsetAllocationAllowedCodeRule(
    var allowed: MutableList<String>,
) : BatchedRouteOffsetAllocationRule {
    override fun check(item: BatchedRouteOffsetAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRouteOffsetAllocationCodeLengthRule(var maxLength: Int) : BatchedRouteOffsetAllocationRule {
    override fun check(item: BatchedRouteOffsetAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched route offset allocation. */
data class BatchedRouteOffsetAllocationValidator(var rules: MutableList<BatchedRouteOffsetAllocationRule>) {
    fun validate(item: BatchedRouteOffsetAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedRouteOffsetAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRouteOffsetAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRouteOffsetAllocationValidator(): BatchedRouteOffsetAllocationValidator {
    return BatchedRouteOffsetAllocationValidator(
        mutableListOf<BatchedRouteOffsetAllocationRule>(
            BatchedRouteOffsetAllocationTitleLengthRule(64),
            BatchedRouteOffsetAllocationMaximumRangeRule(2.6, 1342.9),
            BatchedRouteOffsetAllocationFuelLitersRangeRule(2.1, 1105.6),
            BatchedRouteOffsetAllocationAllowedCodeRule(mutableListOf<String>("manual", "batched", "primary")),
            BatchedRouteOffsetAllocationCodeLengthRule(86400),
        ),
    )
}
