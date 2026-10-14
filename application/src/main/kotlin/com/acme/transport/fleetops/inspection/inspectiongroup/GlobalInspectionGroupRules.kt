package com.acme.transport.fleetops.inspection.inspectiongroup

/** A rule a global inspection group must pass. Check returns its problems, or none. */
interface GlobalInspectionGroupRule {
    fun check(item: GlobalInspectionGroup): MutableList<String>
}

data class GlobalInspectionGroupNameLengthRule(var maxLength: Int) : GlobalInspectionGroupRule {
    override fun check(item: GlobalInspectionGroup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionGroupRequiredCodeRule(var message: String) : GlobalInspectionGroupRule {
    override fun check(item: GlobalInspectionGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionGroupCodePrefixRule(var prefix: String) : GlobalInspectionGroupRule {
    override fun check(item: GlobalInspectionGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionGroupAttemptsRangeRule(var min: Int, var max: Int) : GlobalInspectionGroupRule {
    override fun check(item: GlobalInspectionGroup): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class GlobalInspectionGroupDiscountRangeRule(
    var min: Double,
    var max: Double,
) : GlobalInspectionGroupRule {
    override fun check(item: GlobalInspectionGroup): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class GlobalInspectionGroupEndsAtRangeRule(var min: Int, var max: Int) : GlobalInspectionGroupRule {
    override fun check(item: GlobalInspectionGroup): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global inspection group. */
data class GlobalInspectionGroupValidator(var rules: MutableList<GlobalInspectionGroupRule>) {
    fun validate(item: GlobalInspectionGroup): MutableList<String> {
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

    fun requireValid(item: GlobalInspectionGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalInspectionGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalInspectionGroupValidator(): GlobalInspectionGroupValidator {
    return GlobalInspectionGroupValidator(
        mutableListOf<GlobalInspectionGroupRule>(
            GlobalInspectionGroupNameLengthRule(1000),
            GlobalInspectionGroupRequiredCodeRule("global inspection group needs this field"),
            GlobalInspectionGroupCodePrefixRule("OPS"),
            GlobalInspectionGroupAttemptsRangeRule(80, 120),
            GlobalInspectionGroupDiscountRangeRule(8.4, 4990.0),
            GlobalInspectionGroupEndsAtRangeRule(80, 86400),
        ),
    )
}
