package com.acme.transport.crewing.roster.agreement

/** A rule a capped roster assignment must pass. Check returns its problems, or none. */
interface CappedRosterAssignmentRule {
    fun check(item: CappedRosterAssignment): MutableList<String>
}

data class CappedRosterAssignmentGraceDaysRangeRule(var min: Int, var max: Int) : CappedRosterAssignmentRule {
    override fun check(item: CappedRosterAssignment): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class CappedRosterAssignmentRequiredNameRule(var message: String) : CappedRosterAssignmentRule {
    override fun check(item: CappedRosterAssignment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedRosterAssignmentBudgetRangeRule(
    var min: Double,
    var max: Double,
) : CappedRosterAssignmentRule {
    override fun check(item: CappedRosterAssignment): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedRosterAssignmentNameLengthRule(var maxLength: Int) : CappedRosterAssignmentRule {
    override fun check(item: CappedRosterAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRosterAssignmentRequiredExternalRefRule(var message: String) : CappedRosterAssignmentRule {
    override fun check(item: CappedRosterAssignment): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped roster assignment. */
data class CappedRosterAssignmentValidator(var rules: MutableList<CappedRosterAssignmentRule>) {
    fun validate(item: CappedRosterAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedRosterAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRosterAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRosterAssignmentValidator(): CappedRosterAssignmentValidator {
    return CappedRosterAssignmentValidator(
        mutableListOf<CappedRosterAssignmentRule>(
            CappedRosterAssignmentGraceDaysRangeRule(120, 255),
            CappedRosterAssignmentRequiredNameRule("capped roster assignment needs a value"),
            CappedRosterAssignmentBudgetRangeRule(4.3, 3760.3),
            CappedRosterAssignmentNameLengthRule(80),
            CappedRosterAssignmentRequiredExternalRefRule("capped roster assignment needs a value"),
        ),
    )
}
