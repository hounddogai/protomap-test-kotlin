package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a strict service plan assignment must pass. Check returns its problems, or none. */
interface StrictServicePlanAssignmentRule {
    fun check(item: StrictServicePlanAssignment): MutableList<String>
}

data class StrictServicePlanAssignmentCodePrefixRule(var prefix: String) : StrictServicePlanAssignmentRule {
    override fun check(item: StrictServicePlanAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictServicePlanAssignmentAllowedTitleRule(
    var allowed: MutableList<String>,
) : StrictServicePlanAssignmentRule {
    override fun check(item: StrictServicePlanAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

data class StrictServicePlanAssignmentCodeLengthRule(var maxLength: Int) : StrictServicePlanAssignmentRule {
    override fun check(item: StrictServicePlanAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictServicePlanAssignmentCapacityRangeRule(
    var min: Int,
    var max: Int,
) : StrictServicePlanAssignmentRule {
    override fun check(item: StrictServicePlanAssignment): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictServicePlanAssignmentQuantityRangeRule(
    var min: Int,
    var max: Int,
) : StrictServicePlanAssignmentRule {
    override fun check(item: StrictServicePlanAssignment): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict service plan assignment. */
data class StrictServicePlanAssignmentValidator(var rules: MutableList<StrictServicePlanAssignmentRule>) {
    fun validate(item: StrictServicePlanAssignment): MutableList<String> {
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

    fun requireValid(item: StrictServicePlanAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictServicePlanAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictServicePlanAssignmentValidator(): StrictServicePlanAssignmentValidator {
    return StrictServicePlanAssignmentValidator(
        mutableListOf<StrictServicePlanAssignmentRule>(
            StrictServicePlanAssignmentCodePrefixRule("ACM"),
            StrictServicePlanAssignmentAllowedTitleRule(mutableListOf<String>("legacy")),
            StrictServicePlanAssignmentCodeLengthRule(32),
            StrictServicePlanAssignmentCapacityRangeRule(32, 1000),
            StrictServicePlanAssignmentQuantityRangeRule(120, 86400),
        ),
    )
}
