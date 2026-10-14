package com.acme.transport.network.servicearea.milestone

/** A rule a lenient service area milestone must pass. Check returns its problems, or none. */
interface LenientServiceAreaMilestoneRule {
    fun check(item: LenientServiceAreaMilestone): MutableList<String>
}

data class LenientServiceAreaMilestoneRequiredShiftCodeRule(
    var message: String,
) : LenientServiceAreaMilestoneRule {
    override fun check(item: LenientServiceAreaMilestone): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaMilestoneShiftCodeLengthRule(
    var maxLength: Int,
) : LenientServiceAreaMilestoneRule {
    override fun check(item: LenientServiceAreaMilestone): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaMilestoneRequiredLabelRule(var message: String) : LenientServiceAreaMilestoneRule {
    override fun check(item: LenientServiceAreaMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient service area milestone. */
data class LenientServiceAreaMilestoneValidator(var rules: MutableList<LenientServiceAreaMilestoneRule>) {
    fun validate(item: LenientServiceAreaMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientServiceAreaMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientServiceAreaMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientServiceAreaMilestoneValidator(): LenientServiceAreaMilestoneValidator {
    return LenientServiceAreaMilestoneValidator(
        mutableListOf<LenientServiceAreaMilestoneRule>(
            LenientServiceAreaMilestoneRequiredShiftCodeRule("lenient service area milestone needs a value"),
            LenientServiceAreaMilestoneShiftCodeLengthRule(16),
            LenientServiceAreaMilestoneRequiredLabelRule("lenient service area milestone needs an entry"),
        ),
    )
}
