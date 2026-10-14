package com.acme.transport.crewing.roster.plan

import com.acme.transport.kit.text.normalize

/** A rule a batched roster mapping must pass. Check returns its problems, or none. */
interface BatchedRosterMappingRule {
    fun check(item: BatchedRosterMapping): MutableList<String>
}

data class BatchedRosterMappingRequiredRegionRule(var message: String) : BatchedRosterMappingRule {
    override fun check(item: BatchedRosterMapping): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterMappingMinimumRangeRule(var min: Double, var max: Double) : BatchedRosterMappingRule {
    override fun check(item: BatchedRosterMapping): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class BatchedRosterMappingAllowedDriverIdRule(
    var allowed: MutableList<String>,
) : BatchedRosterMappingRule {
    override fun check(item: BatchedRosterMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is too long")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterMappingCodePrefixRule(var prefix: String) : BatchedRosterMappingRule {
    override fun check(item: BatchedRosterMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterMappingDriverIdLengthRule(var maxLength: Int) : BatchedRosterMappingRule {
    override fun check(item: BatchedRosterMapping): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterMappingRequiredDriverIdRule(var message: String) : BatchedRosterMappingRule {
    override fun check(item: BatchedRosterMapping): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched roster mapping. */
data class BatchedRosterMappingValidator(var rules: MutableList<BatchedRosterMappingRule>) {
    fun validate(item: BatchedRosterMapping): MutableList<String> {
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

    fun requireValid(item: BatchedRosterMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRosterMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRosterMappingValidator(): BatchedRosterMappingValidator {
    return BatchedRosterMappingValidator(
        mutableListOf<BatchedRosterMappingRule>(
            BatchedRosterMappingRequiredRegionRule("batched roster mapping needs an entry"),
            BatchedRosterMappingMinimumRangeRule(8.9, 4343.0),
            BatchedRosterMappingAllowedDriverIdRule(mutableListOf<String>("scheduled")),
            BatchedRosterMappingCodePrefixRule("Q"),
            BatchedRosterMappingDriverIdLengthRule(3600),
            BatchedRosterMappingRequiredDriverIdRule("batched roster mapping needs a value"),
        ),
    )
}
