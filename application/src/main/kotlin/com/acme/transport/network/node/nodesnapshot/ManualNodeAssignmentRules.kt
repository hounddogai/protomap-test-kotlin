package com.acme.transport.network.node.nodesnapshot

/** A rule a manual node assignment must pass. Check returns its problems, or none. */
interface ManualNodeAssignmentRule {
    fun check(item: ManualNodeAssignment): MutableList<String>
}

data class ManualNodeAssignmentRequiredLabelRule(var message: String) : ManualNodeAssignmentRule {
    override fun check(item: ManualNodeAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualNodeAssignmentRequiredLaneRule(var message: String) : ManualNodeAssignmentRule {
    override fun check(item: ManualNodeAssignment): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualNodeAssignmentCodePrefixRule(var prefix: String) : ManualNodeAssignmentRule {
    override fun check(item: ManualNodeAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualNodeAssignmentCapacityRangeRule(var min: Int, var max: Int) : ManualNodeAssignmentRule {
    override fun check(item: ManualNodeAssignment): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualNodeAssignmentMarginRangeRule(var min: Double, var max: Double) : ManualNodeAssignmentRule {
    override fun check(item: ManualNodeAssignment): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual node assignment. */
data class ManualNodeAssignmentValidator(var rules: MutableList<ManualNodeAssignmentRule>) {
    fun validate(item: ManualNodeAssignment): MutableList<String> {
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

    fun requireValid(item: ManualNodeAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualNodeAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualNodeAssignmentValidator(): ManualNodeAssignmentValidator {
    return ManualNodeAssignmentValidator(
        mutableListOf<ManualNodeAssignmentRule>(
            ManualNodeAssignmentRequiredLabelRule("manual node assignment needs a value"),
            ManualNodeAssignmentRequiredLaneRule("manual node assignment needs an entry"),
            ManualNodeAssignmentCodePrefixRule("REF"),
            ManualNodeAssignmentCapacityRangeRule(16, 120),
            ManualNodeAssignmentMarginRangeRule(2.3, 1547.3),
        ),
    )
}
