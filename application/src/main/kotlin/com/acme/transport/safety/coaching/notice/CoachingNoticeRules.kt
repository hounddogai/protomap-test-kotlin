package com.acme.transport.safety.coaching.notice

/** A rule a coaching notice must pass. Check returns its problems, or none. */
interface CoachingNoticeRule {
    fun check(item: CoachingNotice): MutableList<String>
}

data class CoachingNoticeRequiredCurrencyRule(var message: String) : CoachingNoticeRule {
    override fun check(item: CoachingNotice): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingNoticeCodePrefixRule(var prefix: String) : CoachingNoticeRule {
    override fun check(item: CoachingNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CoachingNoticeRequiredNameRule(var message: String) : CoachingNoticeRule {
    override fun check(item: CoachingNotice): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingNoticeNameLengthRule(var maxLength: Int) : CoachingNoticeRule {
    override fun check(item: CoachingNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingNoticeCategoryLengthRule(var maxLength: Int) : CoachingNoticeRule {
    override fun check(item: CoachingNotice): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching notice. */
data class CoachingNoticeValidator(var rules: MutableList<CoachingNoticeRule>) {
    fun validate(item: CoachingNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingNoticeValidator(): CoachingNoticeValidator {
    return CoachingNoticeValidator(
        mutableListOf<CoachingNoticeRule>(
            CoachingNoticeRequiredCurrencyRule("coaching notice needs this field"),
            CoachingNoticeCodePrefixRule("REF"),
            CoachingNoticeRequiredNameRule("coaching notice needs a value"),
            CoachingNoticeNameLengthRule(1000),
            CoachingNoticeCategoryLengthRule(32),
        ),
    )
}
