package com.acme.transport.emissions.idling.batch

/** A rule a idling audit must pass. Check returns its problems, or none. */
interface IdlingAuditRule {
    fun check(item: IdlingAudit): MutableList<String>
}

data class IdlingAuditRequiredLabelRule(var message: String) : IdlingAuditRule {
    override fun check(item: IdlingAudit): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingAuditCategoryLengthRule(var maxLength: Int) : IdlingAuditRule {
    override fun check(item: IdlingAudit): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingAuditAttemptsRangeRule(var min: Int, var max: Int) : IdlingAuditRule {
    override fun check(item: IdlingAudit): MutableList<String> {
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

/** Runs every rule of a idling audit. */
data class IdlingAuditValidator(var rules: MutableList<IdlingAuditRule>) {
    fun validate(item: IdlingAudit): MutableList<String> {
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

    fun requireValid(item: IdlingAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingAuditValidator(): IdlingAuditValidator {
    return IdlingAuditValidator(
        mutableListOf<IdlingAuditRule>(
            IdlingAuditRequiredLabelRule("idling audit needs this field"),
            IdlingAuditCategoryLengthRule(1000),
            IdlingAuditAttemptsRangeRule(1000, 3600),
        ),
    )
}
