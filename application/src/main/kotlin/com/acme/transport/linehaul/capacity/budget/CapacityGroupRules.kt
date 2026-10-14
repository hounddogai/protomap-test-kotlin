package com.acme.transport.linehaul.capacity.budget

/** A rule a capacity group must pass. Check returns its problems, or none. */
interface CapacityGroupRule {
    fun check(item: CapacityGroup): MutableList<String>
}

data class CapacityGroupLabelLengthRule(var maxLength: Int) : CapacityGroupRule {
    override fun check(item: CapacityGroup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityGroupStartsAtRangeRule(var min: Int, var max: Int) : CapacityGroupRule {
    override fun check(item: CapacityGroup): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class CapacityGroupCodeLengthRule(var maxLength: Int) : CapacityGroupRule {
    override fun check(item: CapacityGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityGroupApprovedGuardRule(var limit: Double) : CapacityGroupRule {
    override fun check(item: CapacityGroup): MutableList<String> {
        if (item.approved && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity group. */
data class CapacityGroupValidator(var rules: MutableList<CapacityGroupRule>) {
    fun validate(item: CapacityGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityGroupValidator(): CapacityGroupValidator {
    return CapacityGroupValidator(
        mutableListOf<CapacityGroupRule>(
            CapacityGroupLabelLengthRule(64),
            CapacityGroupStartsAtRangeRule(16, 3600),
            CapacityGroupCodeLengthRule(64),
            CapacityGroupApprovedGuardRule(4.9),
        ),
    )
}
