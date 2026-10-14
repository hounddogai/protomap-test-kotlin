package com.acme.transport.linehaul.capacity.milestone

/** A rule a capacity milestone must pass. Check returns its problems, or none. */
interface CapacityMilestoneRule {
    fun check(item: CapacityMilestone): MutableList<String>
}

data class CapacityMilestoneNameLengthRule(var maxLength: Int) : CapacityMilestoneRule {
    override fun check(item: CapacityMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityMilestoneApprovedGuardRule(var limit: Double) : CapacityMilestoneRule {
    override fun check(item: CapacityMilestone): MutableList<String> {
        if (item.approved && item.cost > limit) {
            return mutableListOf<String>("cost ${item.cost} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class CapacityMilestoneFloorValueRangeRule(var min: Double, var max: Double) : CapacityMilestoneRule {
    override fun check(item: CapacityMilestone): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class CapacityMilestoneContactEmailLengthRule(var maxLength: Int) : CapacityMilestoneRule {
    override fun check(item: CapacityMilestone): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity milestone. */
data class CapacityMilestoneValidator(var rules: MutableList<CapacityMilestoneRule>) {
    fun validate(item: CapacityMilestone): MutableList<String> {
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

    fun requireValid(item: CapacityMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityMilestoneValidator(): CapacityMilestoneValidator {
    return CapacityMilestoneValidator(
        mutableListOf<CapacityMilestoneRule>(
            CapacityMilestoneNameLengthRule(86400),
            CapacityMilestoneApprovedGuardRule(5.8),
            CapacityMilestoneFloorValueRangeRule(1.1, 172.1),
            CapacityMilestoneContactEmailLengthRule(16),
        ),
    )
}
