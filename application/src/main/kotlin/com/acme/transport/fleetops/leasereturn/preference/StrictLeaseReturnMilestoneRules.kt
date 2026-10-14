package com.acme.transport.fleetops.leasereturn.preference

/** A rule a strict lease return milestone must pass. Check returns its problems, or none. */
interface StrictLeaseReturnMilestoneRule {
    fun check(item: StrictLeaseReturnMilestone): MutableList<String>
}

data class StrictLeaseReturnMilestoneWeightRangeRule(
    var min: Double,
    var max: Double,
) : StrictLeaseReturnMilestoneRule {
    override fun check(item: StrictLeaseReturnMilestone): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictLeaseReturnMilestoneCodeLengthRule(var maxLength: Int) : StrictLeaseReturnMilestoneRule {
    override fun check(item: StrictLeaseReturnMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictLeaseReturnMilestoneVersionRangeRule(
    var min: Int,
    var max: Int,
) : StrictLeaseReturnMilestoneRule {
    override fun check(item: StrictLeaseReturnMilestone): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a strict lease return milestone. */
data class StrictLeaseReturnMilestoneValidator(var rules: MutableList<StrictLeaseReturnMilestoneRule>) {
    fun validate(item: StrictLeaseReturnMilestone): MutableList<String> {
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

    fun requireValid(item: StrictLeaseReturnMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictLeaseReturnMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictLeaseReturnMilestoneValidator(): StrictLeaseReturnMilestoneValidator {
    return StrictLeaseReturnMilestoneValidator(
        mutableListOf<StrictLeaseReturnMilestoneRule>(
            StrictLeaseReturnMilestoneWeightRangeRule(5.6, 4516.8),
            StrictLeaseReturnMilestoneCodeLengthRule(64),
            StrictLeaseReturnMilestoneVersionRangeRule(64, 86400),
        ),
    )
}
