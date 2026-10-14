package com.acme.transport.terminal.dwell.cycle

/** A rule a dwell assignment must pass. Check returns its problems, or none. */
interface DwellAssignmentRule {
    fun check(item: DwellAssignment): MutableList<String>
}

data class DwellAssignmentDiscountRangeRule(var min: Double, var max: Double) : DwellAssignmentRule {
    override fun check(item: DwellAssignment): MutableList<String> {
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

data class DwellAssignmentCodePrefixRule(var prefix: String) : DwellAssignmentRule {
    override fun check(item: DwellAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DwellAssignmentVersionRangeRule(var min: Int, var max: Int) : DwellAssignmentRule {
    override fun check(item: DwellAssignment): MutableList<String> {
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

data class DwellAssignmentPinnedGuardRule(var limit: Double) : DwellAssignmentRule {
    override fun check(item: DwellAssignment): MutableList<String> {
        if (item.pinned && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell assignment. */
data class DwellAssignmentValidator(var rules: MutableList<DwellAssignmentRule>) {
    fun validate(item: DwellAssignment): MutableList<String> {
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

    fun requireValid(item: DwellAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellAssignmentValidator(): DwellAssignmentValidator {
    return DwellAssignmentValidator(
        mutableListOf<DwellAssignmentRule>(
            DwellAssignmentDiscountRangeRule(0.3, 2455.8),
            DwellAssignmentCodePrefixRule("ACM"),
            DwellAssignmentVersionRangeRule(3600, 86400),
            DwellAssignmentPinnedGuardRule(0.4),
        ),
    )
}
