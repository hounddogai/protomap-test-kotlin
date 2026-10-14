package com.acme.transport.terminal.dwell.plan

/** A rule a regional dwell plan must pass. Check returns its problems, or none. */
interface RegionalDwellPlanRule {
    fun check(item: RegionalDwellPlan): MutableList<String>
}

data class RegionalDwellPlanTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RegionalDwellPlanRule {
    override fun check(item: RegionalDwellPlan): MutableList<String> {
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

data class RegionalDwellPlanCeilingRangeRule(var min: Double, var max: Double) : RegionalDwellPlanRule {
    override fun check(item: RegionalDwellPlan): MutableList<String> {
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

data class RegionalDwellPlanTitleLengthRule(var maxLength: Int) : RegionalDwellPlanRule {
    override fun check(item: RegionalDwellPlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDwellPlanPhoneLengthRule(var maxLength: Int) : RegionalDwellPlanRule {
    override fun check(item: RegionalDwellPlan): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDwellPlanRequiredPhoneRule(var message: String) : RegionalDwellPlanRule {
    override fun check(item: RegionalDwellPlan): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDwellPlanStopCountRangeRule(var min: Int, var max: Int) : RegionalDwellPlanRule {
    override fun check(item: RegionalDwellPlan): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional dwell plan. */
data class RegionalDwellPlanValidator(var rules: MutableList<RegionalDwellPlanRule>) {
    fun validate(item: RegionalDwellPlan): MutableList<String> {
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

    fun requireValid(item: RegionalDwellPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDwellPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDwellPlanValidator(): RegionalDwellPlanValidator {
    return RegionalDwellPlanValidator(
        mutableListOf<RegionalDwellPlanRule>(
            RegionalDwellPlanTagsLimitRule(3600, mutableListOf<String>("manual", "adaptive")),
            RegionalDwellPlanCeilingRangeRule(5.5, 911.5),
            RegionalDwellPlanTitleLengthRule(32),
            RegionalDwellPlanPhoneLengthRule(80),
            RegionalDwellPlanRequiredPhoneRule("regional dwell plan needs this field"),
            RegionalDwellPlanStopCountRangeRule(80, 80),
        ),
    )
}
