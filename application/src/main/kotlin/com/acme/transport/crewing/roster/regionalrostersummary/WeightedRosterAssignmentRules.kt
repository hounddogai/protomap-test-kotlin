package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.kit.text.normalize

/** A rule a weighted roster assignment must pass. Check returns its problems, or none. */
interface WeightedRosterAssignmentRule {
    fun check(item: WeightedRosterAssignment): MutableList<String>
}

data class WeightedRosterAssignmentRequiredTitleRule(var message: String) : WeightedRosterAssignmentRule {
    override fun check(item: WeightedRosterAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRosterAssignmentPinnedGuardRule(var limit: Int) : WeightedRosterAssignmentRule {
    override fun check(item: WeightedRosterAssignment): MutableList<String> {
        if (item.pinned && item.capacity > limit) {
            return mutableListOf<String>("capacity ${item.capacity} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRosterAssignmentCapacityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedRosterAssignmentRule {
    override fun check(item: WeightedRosterAssignment): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class WeightedRosterAssignmentAllowedTitleRule(
    var allowed: MutableList<String>,
) : WeightedRosterAssignmentRule {
    override fun check(item: WeightedRosterAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRosterAssignmentDepotLengthRule(var maxLength: Int) : WeightedRosterAssignmentRule {
    override fun check(item: WeightedRosterAssignment): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRosterAssignmentTitleLengthRule(var maxLength: Int) : WeightedRosterAssignmentRule {
    override fun check(item: WeightedRosterAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted roster assignment. */
data class WeightedRosterAssignmentValidator(var rules: MutableList<WeightedRosterAssignmentRule>) {
    fun validate(item: WeightedRosterAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedRosterAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRosterAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRosterAssignmentValidator(): WeightedRosterAssignmentValidator {
    return WeightedRosterAssignmentValidator(
        mutableListOf<WeightedRosterAssignmentRule>(
            WeightedRosterAssignmentRequiredTitleRule("weighted roster assignment needs a value"),
            WeightedRosterAssignmentPinnedGuardRule(255),
            WeightedRosterAssignmentCapacityRangeRule(16, 255),
            WeightedRosterAssignmentAllowedTitleRule(mutableListOf<String>()),
            WeightedRosterAssignmentDepotLengthRule(80),
            WeightedRosterAssignmentTitleLengthRule(3600),
        ),
    )
}
