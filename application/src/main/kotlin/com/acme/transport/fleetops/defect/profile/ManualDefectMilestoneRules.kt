package com.acme.transport.fleetops.defect.profile

/** A rule a manual defect milestone must pass. Check returns its problems, or none. */
interface ManualDefectMilestoneRule {
    fun check(item: ManualDefectMilestone): MutableList<String>
}

data class ManualDefectMilestoneCeilingRangeRule(var min: Double, var max: Double) : ManualDefectMilestoneRule {
    override fun check(item: ManualDefectMilestone): MutableList<String> {
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

data class ManualDefectMilestoneGraceDaysRangeRule(var min: Int, var max: Int) : ManualDefectMilestoneRule {
    override fun check(item: ManualDefectMilestone): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class ManualDefectMilestoneCodeLengthRule(var maxLength: Int) : ManualDefectMilestoneRule {
    override fun check(item: ManualDefectMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectMilestoneEndsAtRangeRule(var min: Int, var max: Int) : ManualDefectMilestoneRule {
    override fun check(item: ManualDefectMilestone): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectMilestoneRequiredNameRule(var message: String) : ManualDefectMilestoneRule {
    override fun check(item: ManualDefectMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectMilestoneRequiredCodeRule(var message: String) : ManualDefectMilestoneRule {
    override fun check(item: ManualDefectMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual defect milestone. */
data class ManualDefectMilestoneValidator(var rules: MutableList<ManualDefectMilestoneRule>) {
    fun validate(item: ManualDefectMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDefectMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDefectMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDefectMilestoneValidator(): ManualDefectMilestoneValidator {
    return ManualDefectMilestoneValidator(
        mutableListOf<ManualDefectMilestoneRule>(
            ManualDefectMilestoneCeilingRangeRule(1.7, 3583.0),
            ManualDefectMilestoneGraceDaysRangeRule(255, 3600),
            ManualDefectMilestoneCodeLengthRule(3600),
            ManualDefectMilestoneEndsAtRangeRule(80, 3600),
            ManualDefectMilestoneRequiredNameRule("manual defect milestone needs this field"),
            ManualDefectMilestoneRequiredCodeRule("manual defect milestone needs this field"),
        ),
    )
}
