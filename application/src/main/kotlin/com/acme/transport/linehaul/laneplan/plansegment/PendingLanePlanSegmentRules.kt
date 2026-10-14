package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.text.normalize

/** A rule a pending lane plan segment must pass. Check returns its problems, or none. */
interface PendingLanePlanSegmentRule {
    fun check(item: PendingLanePlanSegment): MutableList<String>
}

data class PendingLanePlanSegmentAllowedNotesRule(
    var allowed: MutableList<String>,
) : PendingLanePlanSegmentRule {
    override fun check(item: PendingLanePlanSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanSegmentActiveGuardRule(var limit: Int) : PendingLanePlanSegmentRule {
    override fun check(item: PendingLanePlanSegment): MutableList<String> {
        if (item.active && item.createdAt > limit) {
            return mutableListOf<String>("created at ${item.createdAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanSegmentCreatedAtRangeRule(var min: Int, var max: Int) : PendingLanePlanSegmentRule {
    override fun check(item: PendingLanePlanSegment): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanSegmentFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : PendingLanePlanSegmentRule {
    override fun check(item: PendingLanePlanSegment): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanSegmentRequiredTitleRule(var message: String) : PendingLanePlanSegmentRule {
    override fun check(item: PendingLanePlanSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending lane plan segment. */
data class PendingLanePlanSegmentValidator(var rules: MutableList<PendingLanePlanSegmentRule>) {
    fun validate(item: PendingLanePlanSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingLanePlanSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingLanePlanSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingLanePlanSegmentValidator(): PendingLanePlanSegmentValidator {
    return PendingLanePlanSegmentValidator(
        mutableListOf<PendingLanePlanSegmentRule>(
            PendingLanePlanSegmentAllowedNotesRule(mutableListOf<String>("primary", "scheduled", "tiered")),
            PendingLanePlanSegmentActiveGuardRule(255),
            PendingLanePlanSegmentCreatedAtRangeRule(32, 120),
            PendingLanePlanSegmentFuelLitersRangeRule(9.9, 2891.2),
            PendingLanePlanSegmentRequiredTitleRule("pending lane plan segment needs a value"),
        ),
    )
}
