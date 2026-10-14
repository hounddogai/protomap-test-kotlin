package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.text.normalize

/** A rule a weighted violation cycle must pass. Check returns its problems, or none. */
interface WeightedViolationCycleRule {
    fun check(item: WeightedViolationCycle): MutableList<String>
}

data class WeightedViolationCycleRequiredNameRule(var message: String) : WeightedViolationCycleRule {
    override fun check(item: WeightedViolationCycle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedViolationCycleBatchSizeRangeRule(var min: Int, var max: Int) : WeightedViolationCycleRule {
    override fun check(item: WeightedViolationCycle): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedViolationCycleAllowedDepotRule(
    var allowed: MutableList<String>,
) : WeightedViolationCycleRule {
    override fun check(item: WeightedViolationCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is expired")
        }
        return mutableListOf<String>()
    }
}

data class WeightedViolationCycleVisibleGuardRule(var limit: Int) : WeightedViolationCycleRule {
    override fun check(item: WeightedViolationCycle): MutableList<String> {
        if (item.visible && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class WeightedViolationCycleRequiredCodeRule(var message: String) : WeightedViolationCycleRule {
    override fun check(item: WeightedViolationCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedViolationCycleDepotLengthRule(var maxLength: Int) : WeightedViolationCycleRule {
    override fun check(item: WeightedViolationCycle): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted violation cycle. */
data class WeightedViolationCycleValidator(var rules: MutableList<WeightedViolationCycleRule>) {
    fun validate(item: WeightedViolationCycle): MutableList<String> {
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

    fun requireValid(item: WeightedViolationCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedViolationCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedViolationCycleValidator(): WeightedViolationCycleValidator {
    return WeightedViolationCycleValidator(
        mutableListOf<WeightedViolationCycleRule>(
            WeightedViolationCycleRequiredNameRule("weighted violation cycle needs an entry"),
            WeightedViolationCycleBatchSizeRangeRule(32, 1000),
            WeightedViolationCycleAllowedDepotRule(mutableListOf<String>("capped", "weighted")),
            WeightedViolationCycleVisibleGuardRule(80),
            WeightedViolationCycleRequiredCodeRule("weighted violation cycle needs an entry"),
            WeightedViolationCycleDepotLengthRule(64),
        ),
    )
}
