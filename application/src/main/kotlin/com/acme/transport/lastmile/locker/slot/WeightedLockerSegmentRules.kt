package com.acme.transport.lastmile.locker.slot

import com.acme.transport.kit.text.normalize

/** A rule a weighted locker segment must pass. Check returns its problems, or none. */
interface WeightedLockerSegmentRule {
    fun check(item: WeightedLockerSegment): MutableList<String>
}

data class WeightedLockerSegmentRequiredCarrierRule(var message: String) : WeightedLockerSegmentRule {
    override fun check(item: WeightedLockerSegment): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedLockerSegmentAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : WeightedLockerSegmentRule {
    override fun check(item: WeightedLockerSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is missing")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLockerSegmentRequiredNameRule(var message: String) : WeightedLockerSegmentRule {
    override fun check(item: WeightedLockerSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted locker segment. */
data class WeightedLockerSegmentValidator(var rules: MutableList<WeightedLockerSegmentRule>) {
    fun validate(item: WeightedLockerSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedLockerSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedLockerSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedLockerSegmentValidator(): WeightedLockerSegmentValidator {
    return WeightedLockerSegmentValidator(
        mutableListOf<WeightedLockerSegmentRule>(
            WeightedLockerSegmentRequiredCarrierRule("weighted locker segment needs this field"),
            WeightedLockerSegmentAllowedDescriptionRule(mutableListOf<String>("adaptive", "pending", "default")),
            WeightedLockerSegmentRequiredNameRule("weighted locker segment needs an entry"),
        ),
    )
}
