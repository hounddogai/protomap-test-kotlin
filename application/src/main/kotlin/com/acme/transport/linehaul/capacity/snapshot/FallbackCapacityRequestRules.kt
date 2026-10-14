package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a fallback capacity request must pass. Check returns its problems, or none. */
interface FallbackCapacityRequestRule {
    fun check(item: FallbackCapacityRequest): MutableList<String>
}

data class FallbackCapacityRequestRatioRangeRule(
    var min: Double,
    var max: Double,
) : FallbackCapacityRequestRule {
    override fun check(item: FallbackCapacityRequest): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCapacityRequestCodePrefixRule(var prefix: String) : FallbackCapacityRequestRule {
    override fun check(item: FallbackCapacityRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCapacityRequestCeilingRangeRule(
    var min: Double,
    var max: Double,
) : FallbackCapacityRequestRule {
    override fun check(item: FallbackCapacityRequest): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCapacityRequestRouteCodeLengthRule(var maxLength: Int) : FallbackCapacityRequestRule {
    override fun check(item: FallbackCapacityRequest): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCapacityRequestAllowedCategoryRule(
    var allowed: MutableList<String>,
) : FallbackCapacityRequestRule {
    override fun check(item: FallbackCapacityRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCapacityRequestRequiredRegionRule(var message: String) : FallbackCapacityRequestRule {
    override fun check(item: FallbackCapacityRequest): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback capacity request. */
data class FallbackCapacityRequestValidator(var rules: MutableList<FallbackCapacityRequestRule>) {
    fun validate(item: FallbackCapacityRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackCapacityRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCapacityRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCapacityRequestValidator(): FallbackCapacityRequestValidator {
    return FallbackCapacityRequestValidator(
        mutableListOf<FallbackCapacityRequestRule>(
            FallbackCapacityRequestRatioRangeRule(8.9, 4359.8),
            FallbackCapacityRequestCodePrefixRule("ACM"),
            FallbackCapacityRequestCeilingRangeRule(1.8, 694.0),
            FallbackCapacityRequestRouteCodeLengthRule(16),
            FallbackCapacityRequestAllowedCategoryRule(mutableListOf<String>("pending", "tiered")),
            FallbackCapacityRequestRequiredRegionRule("fallback capacity request needs this field"),
        ),
    )
}
