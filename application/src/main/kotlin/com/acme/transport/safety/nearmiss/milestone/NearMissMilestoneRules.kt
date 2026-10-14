package com.acme.transport.safety.nearmiss.milestone

/** A rule a near miss milestone must pass. Check returns its problems, or none. */
interface NearMissMilestoneRule {
    fun check(item: NearMissMilestone): MutableList<String>
}

data class NearMissMilestoneAmountRangeRule(var min: Double, var max: Double) : NearMissMilestoneRule {
    override fun check(item: NearMissMilestone): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NearMissMilestoneRequiredCodeRule(var message: String) : NearMissMilestoneRule {
    override fun check(item: NearMissMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissMilestoneRequiredLabelRule(var message: String) : NearMissMilestoneRule {
    override fun check(item: NearMissMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissMilestoneCodeLengthRule(var maxLength: Int) : NearMissMilestoneRule {
    override fun check(item: NearMissMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissMilestoneCreatedAtRangeRule(var min: Int, var max: Int) : NearMissMilestoneRule {
    override fun check(item: NearMissMilestone): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class NearMissMilestoneFloorValueRangeRule(var min: Double, var max: Double) : NearMissMilestoneRule {
    override fun check(item: NearMissMilestone): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss milestone. */
data class NearMissMilestoneValidator(var rules: MutableList<NearMissMilestoneRule>) {
    fun validate(item: NearMissMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NearMissMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissMilestoneValidator(): NearMissMilestoneValidator {
    return NearMissMilestoneValidator(
        mutableListOf<NearMissMilestoneRule>(
            NearMissMilestoneAmountRangeRule(8.8, 2835.4),
            NearMissMilestoneRequiredCodeRule("near miss milestone needs an entry"),
            NearMissMilestoneRequiredLabelRule("near miss milestone needs a value"),
            NearMissMilestoneCodeLengthRule(64),
            NearMissMilestoneCreatedAtRangeRule(16, 255),
            NearMissMilestoneFloorValueRangeRule(3.7, 3071.2),
        ),
    )
}
