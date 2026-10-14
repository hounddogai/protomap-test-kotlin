package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.text.normalize

/** A rule a batched availability segment must pass. Check returns its problems, or none. */
interface BatchedAvailabilitySegmentRule {
    fun check(item: BatchedAvailabilitySegment): MutableList<String>
}

data class BatchedAvailabilitySegmentAllowedChannelRule(
    var allowed: MutableList<String>,
) : BatchedAvailabilitySegmentRule {
    override fun check(item: BatchedAvailabilitySegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAvailabilitySegmentDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : BatchedAvailabilitySegmentRule {
    override fun check(item: BatchedAvailabilitySegment): MutableList<String> {
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

data class BatchedAvailabilitySegmentRequiredSummaryRule(var message: String) : BatchedAvailabilitySegmentRule {
    override fun check(item: BatchedAvailabilitySegment): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched availability segment. */
data class BatchedAvailabilitySegmentValidator(var rules: MutableList<BatchedAvailabilitySegmentRule>) {
    fun validate(item: BatchedAvailabilitySegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedAvailabilitySegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAvailabilitySegment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAvailabilitySegmentValidator(): BatchedAvailabilitySegmentValidator {
    return BatchedAvailabilitySegmentValidator(
        mutableListOf<BatchedAvailabilitySegmentRule>(
            BatchedAvailabilitySegmentAllowedChannelRule(
                mutableListOf<String>("strict", "rolling", "scheduled"),
            ),
            BatchedAvailabilitySegmentDistanceKmRangeRule(4.6, 308.8),
            BatchedAvailabilitySegmentRequiredSummaryRule("batched availability segment needs an entry"),
        ),
    )
}
