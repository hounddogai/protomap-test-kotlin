package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.kit.text.normalize

/** A rule a batched route offset mapping must pass. Check returns its problems, or none. */
interface BatchedRouteOffsetMappingRule {
    fun check(item: BatchedRouteOffsetMapping): MutableList<String>
}

data class BatchedRouteOffsetMappingRequiredCodeRule(var message: String) : BatchedRouteOffsetMappingRule {
    override fun check(item: BatchedRouteOffsetMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRouteOffsetMappingRequiredNameRule(var message: String) : BatchedRouteOffsetMappingRule {
    override fun check(item: BatchedRouteOffsetMapping): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRouteOffsetMappingAllowedCarrierRule(
    var allowed: MutableList<String>,
) : BatchedRouteOffsetMappingRule {
    override fun check(item: BatchedRouteOffsetMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is too long")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRouteOffsetMappingCostRangeRule(
    var min: Double,
    var max: Double,
) : BatchedRouteOffsetMappingRule {
    override fun check(item: BatchedRouteOffsetMapping): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class BatchedRouteOffsetMappingCodeLengthRule(var maxLength: Int) : BatchedRouteOffsetMappingRule {
    override fun check(item: BatchedRouteOffsetMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRouteOffsetMappingExternalRefLengthRule(var maxLength: Int) : BatchedRouteOffsetMappingRule {
    override fun check(item: BatchedRouteOffsetMapping): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched route offset mapping. */
data class BatchedRouteOffsetMappingValidator(var rules: MutableList<BatchedRouteOffsetMappingRule>) {
    fun validate(item: BatchedRouteOffsetMapping): MutableList<String> {
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

    fun requireValid(item: BatchedRouteOffsetMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRouteOffsetMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRouteOffsetMappingValidator(): BatchedRouteOffsetMappingValidator {
    return BatchedRouteOffsetMappingValidator(
        mutableListOf<BatchedRouteOffsetMappingRule>(
            BatchedRouteOffsetMappingRequiredCodeRule("batched route offset mapping needs this field"),
            BatchedRouteOffsetMappingRequiredNameRule("batched route offset mapping needs an entry"),
            BatchedRouteOffsetMappingAllowedCarrierRule(mutableListOf<String>()),
            BatchedRouteOffsetMappingCostRangeRule(3.3, 3108.9),
            BatchedRouteOffsetMappingCodeLengthRule(64),
            BatchedRouteOffsetMappingExternalRefLengthRule(80),
        ),
    )
}
