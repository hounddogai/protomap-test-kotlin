package com.acme.transport.terminal.dwell.milestone

/** A rule a lenient dwell milestone must pass. Check returns its problems, or none. */
interface LenientDwellMilestoneRule {
    fun check(item: LenientDwellMilestone): MutableList<String>
}

data class LenientDwellMilestoneRequiredTrailerIdRule(var message: String) : LenientDwellMilestoneRule {
    override fun check(item: LenientDwellMilestone): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellMilestoneTrailerIdLengthRule(var maxLength: Int) : LenientDwellMilestoneRule {
    override fun check(item: LenientDwellMilestone): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellMilestoneRequiredLabelRule(var message: String) : LenientDwellMilestoneRule {
    override fun check(item: LenientDwellMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient dwell milestone. */
data class LenientDwellMilestoneValidator(var rules: MutableList<LenientDwellMilestoneRule>) {
    fun validate(item: LenientDwellMilestone): MutableList<String> {
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

    fun requireValid(item: LenientDwellMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDwellMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDwellMilestoneValidator(): LenientDwellMilestoneValidator {
    return LenientDwellMilestoneValidator(
        mutableListOf<LenientDwellMilestoneRule>(
            LenientDwellMilestoneRequiredTrailerIdRule("lenient dwell milestone needs an entry"),
            LenientDwellMilestoneTrailerIdLengthRule(255),
            LenientDwellMilestoneRequiredLabelRule("lenient dwell milestone needs a value"),
        ),
    )
}
