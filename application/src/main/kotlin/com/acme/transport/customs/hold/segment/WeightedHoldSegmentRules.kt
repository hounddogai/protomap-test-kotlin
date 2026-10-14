package com.acme.transport.customs.hold.segment

import com.acme.transport.kit.text.normalize

/** A rule a weighted hold segment must pass. Check returns its problems, or none. */
interface WeightedHoldSegmentRule {
    fun check(item: WeightedHoldSegment): MutableList<String>
}

data class WeightedHoldSegmentAllowedLaneRule(var allowed: MutableList<String>) : WeightedHoldSegmentRule {
    override fun check(item: WeightedHoldSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class WeightedHoldSegmentPhoneLengthRule(var maxLength: Int) : WeightedHoldSegmentRule {
    override fun check(item: WeightedHoldSegment): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedHoldSegmentCodeLengthRule(var maxLength: Int) : WeightedHoldSegmentRule {
    override fun check(item: WeightedHoldSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedHoldSegmentArchivedGuardRule(var limit: Int) : WeightedHoldSegmentRule {
    override fun check(item: WeightedHoldSegment): MutableList<String> {
        if (item.archived && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class WeightedHoldSegmentRequiredPhoneRule(var message: String) : WeightedHoldSegmentRule {
    override fun check(item: WeightedHoldSegment): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedHoldSegmentCodePrefixRule(var prefix: String) : WeightedHoldSegmentRule {
    override fun check(item: WeightedHoldSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted hold segment. */
data class WeightedHoldSegmentValidator(var rules: MutableList<WeightedHoldSegmentRule>) {
    fun validate(item: WeightedHoldSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedHoldSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedHoldSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedHoldSegmentValidator(): WeightedHoldSegmentValidator {
    return WeightedHoldSegmentValidator(
        mutableListOf<WeightedHoldSegmentRule>(
            WeightedHoldSegmentAllowedLaneRule(mutableListOf<String>()),
            WeightedHoldSegmentPhoneLengthRule(16),
            WeightedHoldSegmentCodeLengthRule(255),
            WeightedHoldSegmentArchivedGuardRule(120),
            WeightedHoldSegmentRequiredPhoneRule("weighted hold segment needs an entry"),
            WeightedHoldSegmentCodePrefixRule("REF"),
        ),
    )
}
