package com.acme.transport.customs.inspection.budget

/** A rule a primary inspection audit must pass. Check returns its problems, or none. */
interface PrimaryInspectionAuditRule {
    fun check(item: PrimaryInspectionAudit): MutableList<String>
}

data class PrimaryInspectionAuditCodeLengthRule(var maxLength: Int) : PrimaryInspectionAuditRule {
    override fun check(item: PrimaryInspectionAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionAuditRequiredTitleRule(var message: String) : PrimaryInspectionAuditRule {
    override fun check(item: PrimaryInspectionAudit): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionAuditVersionRangeRule(var min: Int, var max: Int) : PrimaryInspectionAuditRule {
    override fun check(item: PrimaryInspectionAudit): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionAuditRequiredUnitRule(var message: String) : PrimaryInspectionAuditRule {
    override fun check(item: PrimaryInspectionAudit): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary inspection audit. */
data class PrimaryInspectionAuditValidator(var rules: MutableList<PrimaryInspectionAuditRule>) {
    fun validate(item: PrimaryInspectionAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryInspectionAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryInspectionAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryInspectionAuditValidator(): PrimaryInspectionAuditValidator {
    return PrimaryInspectionAuditValidator(
        mutableListOf<PrimaryInspectionAuditRule>(
            PrimaryInspectionAuditCodeLengthRule(64),
            PrimaryInspectionAuditRequiredTitleRule("primary inspection audit needs this field"),
            PrimaryInspectionAuditVersionRangeRule(16, 120),
            PrimaryInspectionAuditRequiredUnitRule("primary inspection audit needs this field"),
        ),
    )
}
