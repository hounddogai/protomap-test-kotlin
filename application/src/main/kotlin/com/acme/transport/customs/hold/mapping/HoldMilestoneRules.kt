package com.acme.transport.customs.hold.mapping

/** A rule a hold milestone must pass. Check returns its problems, or none. */
interface HoldMilestoneRule {
    fun check(item: HoldMilestone): MutableList<String>
}

data class HoldMilestoneCountryLengthRule(var maxLength: Int) : HoldMilestoneRule {
    override fun check(item: HoldMilestone): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldMilestoneOwnerIdLengthRule(var maxLength: Int) : HoldMilestoneRule {
    override fun check(item: HoldMilestone): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldMilestoneRequiredNameRule(var message: String) : HoldMilestoneRule {
    override fun check(item: HoldMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldMilestoneCodeLengthRule(var maxLength: Int) : HoldMilestoneRule {
    override fun check(item: HoldMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldMilestoneStopCountRangeRule(var min: Int, var max: Int) : HoldMilestoneRule {
    override fun check(item: HoldMilestone): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class HoldMilestoneStartsAtRangeRule(var min: Int, var max: Int) : HoldMilestoneRule {
    override fun check(item: HoldMilestone): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold milestone. */
data class HoldMilestoneValidator(var rules: MutableList<HoldMilestoneRule>) {
    fun validate(item: HoldMilestone): MutableList<String> {
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

    fun requireValid(item: HoldMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldMilestoneValidator(): HoldMilestoneValidator {
    return HoldMilestoneValidator(
        mutableListOf<HoldMilestoneRule>(
            HoldMilestoneCountryLengthRule(120),
            HoldMilestoneOwnerIdLengthRule(80),
            HoldMilestoneRequiredNameRule("hold milestone needs an entry"),
            HoldMilestoneCodeLengthRule(64),
            HoldMilestoneStopCountRangeRule(32, 3600),
            HoldMilestoneStartsAtRangeRule(255, 86400),
        ),
    )
}
