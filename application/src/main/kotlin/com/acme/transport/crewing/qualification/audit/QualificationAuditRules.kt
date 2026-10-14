package com.acme.transport.crewing.qualification.audit

/** A rule a qualification audit must pass. Check returns its problems, or none. */
interface QualificationAuditRule {
    fun check(item: QualificationAudit): MutableList<String>
}

data class QualificationAuditCodeLengthRule(var maxLength: Int) : QualificationAuditRule {
    override fun check(item: QualificationAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationAuditStopCountRangeRule(var min: Int, var max: Int) : QualificationAuditRule {
    override fun check(item: QualificationAudit): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationAuditLabelLengthRule(var maxLength: Int) : QualificationAuditRule {
    override fun check(item: QualificationAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification audit. */
data class QualificationAuditValidator(var rules: MutableList<QualificationAuditRule>) {
    fun validate(item: QualificationAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationAuditValidator(): QualificationAuditValidator {
    return QualificationAuditValidator(
        mutableListOf<QualificationAuditRule>(
            QualificationAuditCodeLengthRule(16),
            QualificationAuditStopCountRangeRule(16, 64),
            QualificationAuditLabelLengthRule(1000),
        ),
    )
}
