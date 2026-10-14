package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.kit.text.normalize

/** A rule a fallback route offset allocation must pass. Check returns its problems, or none. */
interface FallbackRouteOffsetAllocationRule {
    fun check(item: FallbackRouteOffsetAllocation): MutableList<String>
}

data class FallbackRouteOffsetAllocationRequiredShiftCodeRule(
    var message: String,
) : FallbackRouteOffsetAllocationRule {
    override fun check(item: FallbackRouteOffsetAllocation): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetAllocationShiftCodeLengthRule(
    var maxLength: Int,
) : FallbackRouteOffsetAllocationRule {
    override fun check(item: FallbackRouteOffsetAllocation): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetAllocationBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : FallbackRouteOffsetAllocationRule {
    override fun check(item: FallbackRouteOffsetAllocation): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetAllocationAllowedTitleRule(
    var allowed: MutableList<String>,
) : FallbackRouteOffsetAllocationRule {
    override fun check(item: FallbackRouteOffsetAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetAllocationCodePrefixRule(var prefix: String) : FallbackRouteOffsetAllocationRule {
    override fun check(item: FallbackRouteOffsetAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetAllocationTitleLengthRule(
    var maxLength: Int,
) : FallbackRouteOffsetAllocationRule {
    override fun check(item: FallbackRouteOffsetAllocation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback route offset allocation. */
data class FallbackRouteOffsetAllocationValidator(var rules: MutableList<FallbackRouteOffsetAllocationRule>) {
    fun validate(item: FallbackRouteOffsetAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackRouteOffsetAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRouteOffsetAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRouteOffsetAllocationValidator(): FallbackRouteOffsetAllocationValidator {
    return FallbackRouteOffsetAllocationValidator(
        mutableListOf<FallbackRouteOffsetAllocationRule>(
            FallbackRouteOffsetAllocationRequiredShiftCodeRule("fallback route offset allocation needs a value"),
            FallbackRouteOffsetAllocationShiftCodeLengthRule(64),
            FallbackRouteOffsetAllocationBatchSizeRangeRule(3600, 86400),
            FallbackRouteOffsetAllocationAllowedTitleRule(
                mutableListOf<String>("regional", "regional", "weighted"),
            ),
            FallbackRouteOffsetAllocationCodePrefixRule("X"),
            FallbackRouteOffsetAllocationTitleLengthRule(80),
        ),
    )
}
