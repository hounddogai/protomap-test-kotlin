package com.acme.transport.safety.violation.manifest

/** A rule a adaptive violation audit must pass. Check returns its problems, or none. */
interface AdaptiveViolationAuditRule {
    fun check(item: AdaptiveViolationAudit): MutableList<String>
}

data class AdaptiveViolationAuditRequiredCodeRule(var message: String) : AdaptiveViolationAuditRule {
    override fun check(item: AdaptiveViolationAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveViolationAuditCreatedAtRangeRule(var min: Int, var max: Int) : AdaptiveViolationAuditRule {
    override fun check(item: AdaptiveViolationAudit): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveViolationAuditNameLengthRule(var maxLength: Int) : AdaptiveViolationAuditRule {
    override fun check(item: AdaptiveViolationAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveViolationAuditLabelLengthRule(var maxLength: Int) : AdaptiveViolationAuditRule {
    override fun check(item: AdaptiveViolationAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive violation audit. */
data class AdaptiveViolationAuditValidator(var rules: MutableList<AdaptiveViolationAuditRule>) {
    fun validate(item: AdaptiveViolationAudit): MutableList<String> {
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

    fun requireValid(item: AdaptiveViolationAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveViolationAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveViolationAuditValidator(): AdaptiveViolationAuditValidator {
    return AdaptiveViolationAuditValidator(
        mutableListOf<AdaptiveViolationAuditRule>(
            AdaptiveViolationAuditRequiredCodeRule("adaptive violation audit needs this field"),
            AdaptiveViolationAuditCreatedAtRangeRule(120, 1000),
            AdaptiveViolationAuditNameLengthRule(32),
            AdaptiveViolationAuditLabelLengthRule(3600),
        ),
    )
}
