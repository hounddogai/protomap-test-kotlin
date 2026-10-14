package com.acme.transport.fleetops.defect.defectproposal

/** A rule a fallback defect assignment must pass. Check returns its problems, or none. */
interface FallbackDefectAssignmentRule {
    fun check(item: FallbackDefectAssignment): MutableList<String>
}

data class FallbackDefectAssignmentEndsAtRangeRule(var min: Int, var max: Int) : FallbackDefectAssignmentRule {
    override fun check(item: FallbackDefectAssignment): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDefectAssignmentRequiredNameRule(var message: String) : FallbackDefectAssignmentRule {
    override fun check(item: FallbackDefectAssignment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDefectAssignmentNameLengthRule(var maxLength: Int) : FallbackDefectAssignmentRule {
    override fun check(item: FallbackDefectAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDefectAssignmentCodePrefixRule(var prefix: String) : FallbackDefectAssignmentRule {
    override fun check(item: FallbackDefectAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback defect assignment. */
data class FallbackDefectAssignmentValidator(var rules: MutableList<FallbackDefectAssignmentRule>) {
    fun validate(item: FallbackDefectAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDefectAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDefectAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDefectAssignmentValidator(): FallbackDefectAssignmentValidator {
    return FallbackDefectAssignmentValidator(
        mutableListOf<FallbackDefectAssignmentRule>(
            FallbackDefectAssignmentEndsAtRangeRule(80, 1000),
            FallbackDefectAssignmentRequiredNameRule("fallback defect assignment needs this field"),
            FallbackDefectAssignmentNameLengthRule(16),
            FallbackDefectAssignmentCodePrefixRule("REF"),
        ),
    )
}
