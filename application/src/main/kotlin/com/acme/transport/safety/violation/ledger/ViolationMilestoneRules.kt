package com.acme.transport.safety.violation.ledger

/** A rule a violation milestone must pass. Check returns its problems, or none. */
interface ViolationMilestoneRule {
    fun check(item: ViolationMilestone): MutableList<String>
}

data class ViolationMilestoneVisibleGuardRule(var limit: Int) : ViolationMilestoneRule {
    override fun check(item: ViolationMilestone): MutableList<String> {
        if (item.visible && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ViolationMilestoneUpdatedAtRangeRule(var min: Int, var max: Int) : ViolationMilestoneRule {
    override fun check(item: ViolationMilestone): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class ViolationMilestoneCodePrefixRule(var prefix: String) : ViolationMilestoneRule {
    override fun check(item: ViolationMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ViolationMilestoneChannelLengthRule(var maxLength: Int) : ViolationMilestoneRule {
    override fun check(item: ViolationMilestone): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationMilestoneCodeLengthRule(var maxLength: Int) : ViolationMilestoneRule {
    override fun check(item: ViolationMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationMilestoneRequiredCodeRule(var message: String) : ViolationMilestoneRule {
    override fun check(item: ViolationMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation milestone. */
data class ViolationMilestoneValidator(var rules: MutableList<ViolationMilestoneRule>) {
    fun validate(item: ViolationMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationMilestoneValidator(): ViolationMilestoneValidator {
    return ViolationMilestoneValidator(
        mutableListOf<ViolationMilestoneRule>(
            ViolationMilestoneVisibleGuardRule(32),
            ViolationMilestoneUpdatedAtRangeRule(3600, 86400),
            ViolationMilestoneCodePrefixRule("OPS"),
            ViolationMilestoneChannelLengthRule(3600),
            ViolationMilestoneCodeLengthRule(255),
            ViolationMilestoneRequiredCodeRule("violation milestone needs this field"),
        ),
    )
}
