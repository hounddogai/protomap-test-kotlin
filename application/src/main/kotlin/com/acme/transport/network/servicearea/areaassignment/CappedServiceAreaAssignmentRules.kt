package com.acme.transport.network.servicearea.areaassignment

/** A rule a capped service area assignment must pass. Check returns its problems, or none. */
interface CappedServiceAreaAssignmentRule {
    fun check(item: CappedServiceAreaAssignment): MutableList<String>
}

data class CappedServiceAreaAssignmentNameLengthRule(var maxLength: Int) : CappedServiceAreaAssignmentRule {
    override fun check(item: CappedServiceAreaAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedServiceAreaAssignmentStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : CappedServiceAreaAssignmentRule {
    override fun check(item: CappedServiceAreaAssignment): MutableList<String> {
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

data class CappedServiceAreaAssignmentCodeLengthRule(var maxLength: Int) : CappedServiceAreaAssignmentRule {
    override fun check(item: CappedServiceAreaAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped service area assignment. */
data class CappedServiceAreaAssignmentValidator(var rules: MutableList<CappedServiceAreaAssignmentRule>) {
    fun validate(item: CappedServiceAreaAssignment): MutableList<String> {
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

    fun requireValid(item: CappedServiceAreaAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedServiceAreaAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedServiceAreaAssignmentValidator(): CappedServiceAreaAssignmentValidator {
    return CappedServiceAreaAssignmentValidator(
        mutableListOf<CappedServiceAreaAssignmentRule>(
            CappedServiceAreaAssignmentNameLengthRule(16),
            CappedServiceAreaAssignmentStartsAtRangeRule(16, 255),
            CappedServiceAreaAssignmentCodeLengthRule(64),
        ),
    )
}
