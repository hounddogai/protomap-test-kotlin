package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.kit.text.normalize

/** A rule a primary backhaul milestone must pass. Check returns its problems, or none. */
interface PrimaryBackhaulMilestoneRule {
    fun check(item: PrimaryBackhaulMilestone): MutableList<String>
}

data class PrimaryBackhaulMilestoneAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : PrimaryBackhaulMilestoneRule {
    override fun check(item: PrimaryBackhaulMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBackhaulMilestoneRequiredTimezoneRule(var message: String) : PrimaryBackhaulMilestoneRule {
    override fun check(item: PrimaryBackhaulMilestone): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBackhaulMilestoneCeilingRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryBackhaulMilestoneRule {
    override fun check(item: PrimaryBackhaulMilestone): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class PrimaryBackhaulMilestoneTimezoneLengthRule(var maxLength: Int) : PrimaryBackhaulMilestoneRule {
    override fun check(item: PrimaryBackhaulMilestone): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBackhaulMilestoneNameLengthRule(var maxLength: Int) : PrimaryBackhaulMilestoneRule {
    override fun check(item: PrimaryBackhaulMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBackhaulMilestoneRequiredNameRule(var message: String) : PrimaryBackhaulMilestoneRule {
    override fun check(item: PrimaryBackhaulMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary backhaul milestone. */
data class PrimaryBackhaulMilestoneValidator(var rules: MutableList<PrimaryBackhaulMilestoneRule>) {
    fun validate(item: PrimaryBackhaulMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryBackhaulMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryBackhaulMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryBackhaulMilestoneValidator(): PrimaryBackhaulMilestoneValidator {
    return PrimaryBackhaulMilestoneValidator(
        mutableListOf<PrimaryBackhaulMilestoneRule>(
            PrimaryBackhaulMilestoneAllowedTimezoneRule(mutableListOf<String>("scheduled", "pending", "strict")),
            PrimaryBackhaulMilestoneRequiredTimezoneRule("primary backhaul milestone needs this field"),
            PrimaryBackhaulMilestoneCeilingRangeRule(8.2, 2110.3),
            PrimaryBackhaulMilestoneTimezoneLengthRule(120),
            PrimaryBackhaulMilestoneNameLengthRule(86400),
            PrimaryBackhaulMilestoneRequiredNameRule("primary backhaul milestone needs a value"),
        ),
    )
}
