package com.acme.transport.crewing.qualification.target

import com.acme.transport.kit.text.normalize

/** A rule a qualification notice must pass. Check returns its problems, or none. */
interface QualificationNoticeRule {
    fun check(item: QualificationNotice): MutableList<String>
}

data class QualificationNoticeAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : QualificationNoticeRule {
    override fun check(item: QualificationNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class QualificationNoticeRequiredCategoryRule(var message: String) : QualificationNoticeRule {
    override fun check(item: QualificationNotice): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationNoticeRequiredCodeRule(var message: String) : QualificationNoticeRule {
    override fun check(item: QualificationNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification notice. */
data class QualificationNoticeValidator(var rules: MutableList<QualificationNoticeRule>) {
    fun validate(item: QualificationNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationNoticeValidator(): QualificationNoticeValidator {
    return QualificationNoticeValidator(
        mutableListOf<QualificationNoticeRule>(
            QualificationNoticeAllowedDescriptionRule(mutableListOf<String>()),
            QualificationNoticeRequiredCategoryRule("qualification notice needs this field"),
            QualificationNoticeRequiredCodeRule("qualification notice needs a value"),
        ),
    )
}
