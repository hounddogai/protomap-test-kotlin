package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.kit.text.normalize

/** A rule a weighted rest break allocation must pass. Check returns its problems, or none. */
interface WeightedRestBreakAllocationRule {
    fun check(item: WeightedRestBreakAllocation): MutableList<String>
}

data class WeightedRestBreakAllocationTitleLengthRule(var maxLength: Int) : WeightedRestBreakAllocationRule {
    override fun check(item: WeightedRestBreakAllocation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRestBreakAllocationAllowedCodeRule(
    var allowed: MutableList<String>,
) : WeightedRestBreakAllocationRule {
    override fun check(item: WeightedRestBreakAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRestBreakAllocationRequiredRegionRule(
    var message: String,
) : WeightedRestBreakAllocationRule {
    override fun check(item: WeightedRestBreakAllocation): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRestBreakAllocationDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : WeightedRestBreakAllocationRule {
    override fun check(item: WeightedRestBreakAllocation): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class WeightedRestBreakAllocationRequiredTitleRule(var message: String) : WeightedRestBreakAllocationRule {
    override fun check(item: WeightedRestBreakAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted rest break allocation. */
data class WeightedRestBreakAllocationValidator(var rules: MutableList<WeightedRestBreakAllocationRule>) {
    fun validate(item: WeightedRestBreakAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedRestBreakAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRestBreakAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRestBreakAllocationValidator(): WeightedRestBreakAllocationValidator {
    return WeightedRestBreakAllocationValidator(
        mutableListOf<WeightedRestBreakAllocationRule>(
            WeightedRestBreakAllocationTitleLengthRule(120),
            WeightedRestBreakAllocationAllowedCodeRule(mutableListOf<String>("lenient")),
            WeightedRestBreakAllocationRequiredRegionRule("weighted rest break allocation needs this field"),
            WeightedRestBreakAllocationDistanceKmRangeRule(8.9, 3977.4),
            WeightedRestBreakAllocationRequiredTitleRule("weighted rest break allocation needs this field"),
        ),
    )
}
