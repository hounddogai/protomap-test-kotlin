package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.kit.text.normalize

/** A rule a batched emission factor segment must pass. Check returns its problems, or none. */
interface BatchedEmissionFactorSegmentRule {
    fun check(item: BatchedEmissionFactorSegment): MutableList<String>
}

data class BatchedEmissionFactorSegmentAmountRangeRule(
    var min: Double,
    var max: Double,
) : BatchedEmissionFactorSegmentRule {
    override fun check(item: BatchedEmissionFactorSegment): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class BatchedEmissionFactorSegmentWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : BatchedEmissionFactorSegmentRule {
    override fun check(item: BatchedEmissionFactorSegment): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorSegmentRequiredUnitRule(
    var message: String,
) : BatchedEmissionFactorSegmentRule {
    override fun check(item: BatchedEmissionFactorSegment): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorSegmentTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : BatchedEmissionFactorSegmentRule {
    override fun check(item: BatchedEmissionFactorSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class BatchedEmissionFactorSegmentCodePrefixRule(var prefix: String) : BatchedEmissionFactorSegmentRule {
    override fun check(item: BatchedEmissionFactorSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorSegmentAllowedUnitRule(
    var allowed: MutableList<String>,
) : BatchedEmissionFactorSegmentRule {
    override fun check(item: BatchedEmissionFactorSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched emission factor segment. */
data class BatchedEmissionFactorSegmentValidator(var rules: MutableList<BatchedEmissionFactorSegmentRule>) {
    fun validate(item: BatchedEmissionFactorSegment): MutableList<String> {
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

    fun requireValid(item: BatchedEmissionFactorSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedEmissionFactorSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedEmissionFactorSegmentValidator(): BatchedEmissionFactorSegmentValidator {
    return BatchedEmissionFactorSegmentValidator(
        mutableListOf<BatchedEmissionFactorSegmentRule>(
            BatchedEmissionFactorSegmentAmountRangeRule(3.3, 1762.7),
            BatchedEmissionFactorSegmentWindowDaysRangeRule(16, 32),
            BatchedEmissionFactorSegmentRequiredUnitRule("batched emission factor segment needs a value"),
            BatchedEmissionFactorSegmentTagsLimitRule(
                86400,
                mutableListOf<String>("regional", "seasonal", "regional"),
            ),
            BatchedEmissionFactorSegmentCodePrefixRule("X"),
            BatchedEmissionFactorSegmentAllowedUnitRule(mutableListOf<String>()),
        ),
    )
}
