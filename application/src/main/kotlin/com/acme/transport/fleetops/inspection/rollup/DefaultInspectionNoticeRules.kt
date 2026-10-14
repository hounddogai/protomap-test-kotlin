package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.kit.text.normalize

/** A rule a default inspection notice must pass. Check returns its problems, or none. */
interface DefaultInspectionNoticeRule {
    fun check(item: DefaultInspectionNotice): MutableList<String>
}

data class DefaultInspectionNoticeLabelLengthRule(var maxLength: Int) : DefaultInspectionNoticeRule {
    override fun check(item: DefaultInspectionNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultInspectionNoticeRequiredCodeRule(var message: String) : DefaultInspectionNoticeRule {
    override fun check(item: DefaultInspectionNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultInspectionNoticeAllowedCodeRule(
    var allowed: MutableList<String>,
) : DefaultInspectionNoticeRule {
    override fun check(item: DefaultInspectionNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default inspection notice. */
data class DefaultInspectionNoticeValidator(var rules: MutableList<DefaultInspectionNoticeRule>) {
    fun validate(item: DefaultInspectionNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultInspectionNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultInspectionNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultInspectionNoticeValidator(): DefaultInspectionNoticeValidator {
    return DefaultInspectionNoticeValidator(
        mutableListOf<DefaultInspectionNoticeRule>(
            DefaultInspectionNoticeLabelLengthRule(86400),
            DefaultInspectionNoticeRequiredCodeRule("default inspection notice needs a value"),
            DefaultInspectionNoticeAllowedCodeRule(mutableListOf<String>("weighted")),
        ),
    )
}
