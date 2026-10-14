package com.acme.transport.fleetops.leasereturn.budget

/** A rule a global lease return assignment must pass. Check returns its problems, or none. */
interface GlobalLeaseReturnAssignmentRule {
    fun check(item: GlobalLeaseReturnAssignment): MutableList<String>
}

data class GlobalLeaseReturnAssignmentRequiredChannelRule(
    var message: String,
) : GlobalLeaseReturnAssignmentRule {
    override fun check(item: GlobalLeaseReturnAssignment): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalLeaseReturnAssignmentTitleLengthRule(var maxLength: Int) : GlobalLeaseReturnAssignmentRule {
    override fun check(item: GlobalLeaseReturnAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLeaseReturnAssignmentCodePrefixRule(var prefix: String) : GlobalLeaseReturnAssignmentRule {
    override fun check(item: GlobalLeaseReturnAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLeaseReturnAssignmentStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : GlobalLeaseReturnAssignmentRule {
    override fun check(item: GlobalLeaseReturnAssignment): MutableList<String> {
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

data class GlobalLeaseReturnAssignmentCodeLengthRule(var maxLength: Int) : GlobalLeaseReturnAssignmentRule {
    override fun check(item: GlobalLeaseReturnAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLeaseReturnAssignmentLimitRangeRule(
    var min: Int,
    var max: Int,
) : GlobalLeaseReturnAssignmentRule {
    override fun check(item: GlobalLeaseReturnAssignment): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global lease return assignment. */
data class GlobalLeaseReturnAssignmentValidator(var rules: MutableList<GlobalLeaseReturnAssignmentRule>) {
    fun validate(item: GlobalLeaseReturnAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalLeaseReturnAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalLeaseReturnAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalLeaseReturnAssignmentValidator(): GlobalLeaseReturnAssignmentValidator {
    return GlobalLeaseReturnAssignmentValidator(
        mutableListOf<GlobalLeaseReturnAssignmentRule>(
            GlobalLeaseReturnAssignmentRequiredChannelRule("global lease return assignment needs a value"),
            GlobalLeaseReturnAssignmentTitleLengthRule(64),
            GlobalLeaseReturnAssignmentCodePrefixRule("X"),
            GlobalLeaseReturnAssignmentStartsAtRangeRule(80, 86400),
            GlobalLeaseReturnAssignmentCodeLengthRule(86400),
            GlobalLeaseReturnAssignmentLimitRangeRule(16, 120),
        ),
    )
}
