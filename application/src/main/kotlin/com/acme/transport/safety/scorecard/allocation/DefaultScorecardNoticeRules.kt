package com.acme.transport.safety.scorecard.allocation

/** A rule a default scorecard notice must pass. Check returns its problems, or none. */
interface DefaultScorecardNoticeRule {
    fun check(item: DefaultScorecardNotice): MutableList<String>
}

data class DefaultScorecardNoticeGraceDaysRangeRule(var min: Int, var max: Int) : DefaultScorecardNoticeRule {
    override fun check(item: DefaultScorecardNotice): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardNoticeRequiredTitleRule(var message: String) : DefaultScorecardNoticeRule {
    override fun check(item: DefaultScorecardNotice): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardNoticeContactEmailLengthRule(var maxLength: Int) : DefaultScorecardNoticeRule {
    override fun check(item: DefaultScorecardNotice): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardNoticeCodePrefixRule(var prefix: String) : DefaultScorecardNoticeRule {
    override fun check(item: DefaultScorecardNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardNoticeRequiredContactEmailRule(var message: String) : DefaultScorecardNoticeRule {
    override fun check(item: DefaultScorecardNotice): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default scorecard notice. */
data class DefaultScorecardNoticeValidator(var rules: MutableList<DefaultScorecardNoticeRule>) {
    fun validate(item: DefaultScorecardNotice): MutableList<String> {
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

    fun requireValid(item: DefaultScorecardNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultScorecardNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultScorecardNoticeValidator(): DefaultScorecardNoticeValidator {
    return DefaultScorecardNoticeValidator(
        mutableListOf<DefaultScorecardNoticeRule>(
            DefaultScorecardNoticeGraceDaysRangeRule(32, 1000),
            DefaultScorecardNoticeRequiredTitleRule("default scorecard notice needs this field"),
            DefaultScorecardNoticeContactEmailLengthRule(1000),
            DefaultScorecardNoticeCodePrefixRule("Q"),
            DefaultScorecardNoticeRequiredContactEmailRule("default scorecard notice needs this field"),
        ),
    )
}
