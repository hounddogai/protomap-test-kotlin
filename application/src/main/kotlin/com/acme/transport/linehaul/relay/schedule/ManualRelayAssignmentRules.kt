package com.acme.transport.linehaul.relay.schedule

/** A rule a manual relay assignment must pass. Check returns its problems, or none. */
interface ManualRelayAssignmentRule {
    fun check(item: ManualRelayAssignment): MutableList<String>
}

data class ManualRelayAssignmentCodePrefixRule(var prefix: String) : ManualRelayAssignmentRule {
    override fun check(item: ManualRelayAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualRelayAssignmentTitleLengthRule(var maxLength: Int) : ManualRelayAssignmentRule {
    override fun check(item: ManualRelayAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRelayAssignmentLoadKgRangeRule(var min: Double, var max: Double) : ManualRelayAssignmentRule {
    override fun check(item: ManualRelayAssignment): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual relay assignment. */
data class ManualRelayAssignmentValidator(var rules: MutableList<ManualRelayAssignmentRule>) {
    fun validate(item: ManualRelayAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualRelayAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRelayAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRelayAssignmentValidator(): ManualRelayAssignmentValidator {
    return ManualRelayAssignmentValidator(
        mutableListOf<ManualRelayAssignmentRule>(
            ManualRelayAssignmentCodePrefixRule("OPS"),
            ManualRelayAssignmentTitleLengthRule(255),
            ManualRelayAssignmentLoadKgRangeRule(8.8, 2098.7),
        ),
    )
}
