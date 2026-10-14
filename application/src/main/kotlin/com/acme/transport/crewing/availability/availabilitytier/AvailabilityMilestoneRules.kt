package com.acme.transport.crewing.availability.availabilitytier

/** A rule a availability milestone must pass. Check returns its problems, or none. */
interface AvailabilityMilestoneRule {
    fun check(item: AvailabilityMilestone): MutableList<String>
}

data class AvailabilityMilestoneRequiredSegmentRule(var message: String) : AvailabilityMilestoneRule {
    override fun check(item: AvailabilityMilestone): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityMilestoneRequiredTitleRule(var message: String) : AvailabilityMilestoneRule {
    override fun check(item: AvailabilityMilestone): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityMilestoneFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : AvailabilityMilestoneRule {
    override fun check(item: AvailabilityMilestone): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class AvailabilityMilestoneLoadKgRangeRule(var min: Double, var max: Double) : AvailabilityMilestoneRule {
    override fun check(item: AvailabilityMilestone): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class AvailabilityMilestoneTitleLengthRule(var maxLength: Int) : AvailabilityMilestoneRule {
    override fun check(item: AvailabilityMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityMilestoneCodePrefixRule(var prefix: String) : AvailabilityMilestoneRule {
    override fun check(item: AvailabilityMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability milestone. */
data class AvailabilityMilestoneValidator(var rules: MutableList<AvailabilityMilestoneRule>) {
    fun validate(item: AvailabilityMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilityMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityMilestoneValidator(): AvailabilityMilestoneValidator {
    return AvailabilityMilestoneValidator(
        mutableListOf<AvailabilityMilestoneRule>(
            AvailabilityMilestoneRequiredSegmentRule("availability milestone needs this field"),
            AvailabilityMilestoneRequiredTitleRule("availability milestone needs an entry"),
            AvailabilityMilestoneFuelLitersRangeRule(6.2, 3193.2),
            AvailabilityMilestoneLoadKgRangeRule(7.5, 411.4),
            AvailabilityMilestoneTitleLengthRule(32),
            AvailabilityMilestoneCodePrefixRule("REF"),
        ),
    )
}
