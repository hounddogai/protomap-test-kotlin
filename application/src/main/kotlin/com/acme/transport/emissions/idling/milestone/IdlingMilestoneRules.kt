package com.acme.transport.emissions.idling.milestone

/** A rule a idling milestone must pass. Check returns its problems, or none. */
interface IdlingMilestoneRule {
    fun check(item: IdlingMilestone): MutableList<String>
}

data class IdlingMilestoneTitleLengthRule(var maxLength: Int) : IdlingMilestoneRule {
    override fun check(item: IdlingMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingMilestoneEtaMinutesRangeRule(var min: Int, var max: Int) : IdlingMilestoneRule {
    override fun check(item: IdlingMilestone): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class IdlingMilestoneRequiredTitleRule(var message: String) : IdlingMilestoneRule {
    override fun check(item: IdlingMilestone): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingMilestoneCodeLengthRule(var maxLength: Int) : IdlingMilestoneRule {
    override fun check(item: IdlingMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling milestone. */
data class IdlingMilestoneValidator(var rules: MutableList<IdlingMilestoneRule>) {
    fun validate(item: IdlingMilestone): MutableList<String> {
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

    fun requireValid(item: IdlingMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingMilestoneValidator(): IdlingMilestoneValidator {
    return IdlingMilestoneValidator(
        mutableListOf<IdlingMilestoneRule>(
            IdlingMilestoneTitleLengthRule(64),
            IdlingMilestoneEtaMinutesRangeRule(16, 64),
            IdlingMilestoneRequiredTitleRule("idling milestone needs this field"),
            IdlingMilestoneCodeLengthRule(64),
        ),
    )
}
