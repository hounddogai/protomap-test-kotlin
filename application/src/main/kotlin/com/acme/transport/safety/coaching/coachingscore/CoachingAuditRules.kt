package com.acme.transport.safety.coaching.coachingscore

/** A rule a coaching audit must pass. Check returns its problems, or none. */
interface CoachingAuditRule {
    fun check(item: CoachingAudit): MutableList<String>
}

data class CoachingAuditRequiredOwnerIdRule(var message: String) : CoachingAuditRule {
    override fun check(item: CoachingAudit): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingAuditRequiredTitleRule(var message: String) : CoachingAuditRule {
    override fun check(item: CoachingAudit): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingAuditCodeLengthRule(var maxLength: Int) : CoachingAuditRule {
    override fun check(item: CoachingAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingAuditNameLengthRule(var maxLength: Int) : CoachingAuditRule {
    override fun check(item: CoachingAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingAuditWeightRangeRule(var min: Double, var max: Double) : CoachingAuditRule {
    override fun check(item: CoachingAudit): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class CoachingAuditRequiredCodeRule(var message: String) : CoachingAuditRule {
    override fun check(item: CoachingAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching audit. */
data class CoachingAuditValidator(var rules: MutableList<CoachingAuditRule>) {
    fun validate(item: CoachingAudit): MutableList<String> {
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

    fun requireValid(item: CoachingAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingAuditValidator(): CoachingAuditValidator {
    return CoachingAuditValidator(
        mutableListOf<CoachingAuditRule>(
            CoachingAuditRequiredOwnerIdRule("coaching audit needs a value"),
            CoachingAuditRequiredTitleRule("coaching audit needs this field"),
            CoachingAuditCodeLengthRule(32),
            CoachingAuditNameLengthRule(1000),
            CoachingAuditWeightRangeRule(2.8, 2546.8),
            CoachingAuditRequiredCodeRule("coaching audit needs a value"),
        ),
    )
}
