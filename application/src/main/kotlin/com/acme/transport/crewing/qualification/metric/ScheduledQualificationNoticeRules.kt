package com.acme.transport.crewing.qualification.metric

/** A rule a scheduled qualification notice must pass. Check returns its problems, or none. */
interface ScheduledQualificationNoticeRule {
    fun check(item: ScheduledQualificationNotice): MutableList<String>
}

data class ScheduledQualificationNoticeShiftCodeLengthRule(
    var maxLength: Int,
) : ScheduledQualificationNoticeRule {
    override fun check(item: ScheduledQualificationNotice): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledQualificationNoticeRequiredCategoryRule(
    var message: String,
) : ScheduledQualificationNoticeRule {
    override fun check(item: ScheduledQualificationNotice): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledQualificationNoticeTitleLengthRule(var maxLength: Int) : ScheduledQualificationNoticeRule {
    override fun check(item: ScheduledQualificationNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledQualificationNoticeCodePrefixRule(var prefix: String) : ScheduledQualificationNoticeRule {
    override fun check(item: ScheduledQualificationNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled qualification notice. */
data class ScheduledQualificationNoticeValidator(var rules: MutableList<ScheduledQualificationNoticeRule>) {
    fun validate(item: ScheduledQualificationNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledQualificationNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledQualificationNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledQualificationNoticeValidator(): ScheduledQualificationNoticeValidator {
    return ScheduledQualificationNoticeValidator(
        mutableListOf<ScheduledQualificationNoticeRule>(
            ScheduledQualificationNoticeShiftCodeLengthRule(32),
            ScheduledQualificationNoticeRequiredCategoryRule("scheduled qualification notice needs an entry"),
            ScheduledQualificationNoticeTitleLengthRule(1000),
            ScheduledQualificationNoticeCodePrefixRule("ACM"),
        ),
    )
}
