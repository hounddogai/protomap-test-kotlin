package com.acme.transport.crewing.qualification.proposal

/** A rule a rolling qualification notice must pass. Check returns its problems, or none. */
interface RollingQualificationNoticeRule {
    fun check(item: RollingQualificationNotice): MutableList<String>
}

data class RollingQualificationNoticeLocaleLengthRule(var maxLength: Int) : RollingQualificationNoticeRule {
    override fun check(item: RollingQualificationNotice): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingQualificationNoticeNameLengthRule(var maxLength: Int) : RollingQualificationNoticeRule {
    override fun check(item: RollingQualificationNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingQualificationNoticeCodeLengthRule(var maxLength: Int) : RollingQualificationNoticeRule {
    override fun check(item: RollingQualificationNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling qualification notice. */
data class RollingQualificationNoticeValidator(var rules: MutableList<RollingQualificationNoticeRule>) {
    fun validate(item: RollingQualificationNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingQualificationNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingQualificationNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingQualificationNoticeValidator(): RollingQualificationNoticeValidator {
    return RollingQualificationNoticeValidator(
        mutableListOf<RollingQualificationNoticeRule>(
            RollingQualificationNoticeLocaleLengthRule(64),
            RollingQualificationNoticeNameLengthRule(64),
            RollingQualificationNoticeCodeLengthRule(64),
        ),
    )
}
