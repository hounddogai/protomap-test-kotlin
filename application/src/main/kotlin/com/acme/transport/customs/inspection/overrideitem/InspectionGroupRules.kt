package com.acme.transport.customs.inspection.overrideitem

/** A rule a inspection group must pass. Check returns its problems, or none. */
interface InspectionGroupRule {
    fun check(item: InspectionGroup): MutableList<String>
}

data class InspectionGroupMinimumRangeRule(var min: Double, var max: Double) : InspectionGroupRule {
    override fun check(item: InspectionGroup): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class InspectionGroupLabelLengthRule(var maxLength: Int) : InspectionGroupRule {
    override fun check(item: InspectionGroup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionGroupCodePrefixRule(var prefix: String) : InspectionGroupRule {
    override fun check(item: InspectionGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection group. */
data class InspectionGroupValidator(var rules: MutableList<InspectionGroupRule>) {
    fun validate(item: InspectionGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionGroupValidator(): InspectionGroupValidator {
    return InspectionGroupValidator(
        mutableListOf<InspectionGroupRule>(
            InspectionGroupMinimumRangeRule(0.5, 3999.3),
            InspectionGroupLabelLengthRule(16),
            InspectionGroupCodePrefixRule("Q"),
        ),
    )
}
