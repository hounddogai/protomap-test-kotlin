package com.acme.transport.safety.scorecard.notice

/** A rule a scorecard notice must pass. Check returns its problems, or none. */
interface ScorecardNoticeRule {
    fun check(item: ScorecardNotice): MutableList<String>
}

data class ScorecardNoticeRevisionRangeRule(var min: Int, var max: Int) : ScorecardNoticeRule {
    override fun check(item: ScorecardNotice): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class ScorecardNoticeRequiredCodeRule(var message: String) : ScorecardNoticeRule {
    override fun check(item: ScorecardNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardNoticeRequiredChecksumRule(var message: String) : ScorecardNoticeRule {
    override fun check(item: ScorecardNotice): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardNoticeCodePrefixRule(var prefix: String) : ScorecardNoticeRule {
    override fun check(item: ScorecardNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard notice. */
data class ScorecardNoticeValidator(var rules: MutableList<ScorecardNoticeRule>) {
    fun validate(item: ScorecardNotice): MutableList<String> {
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

    fun requireValid(item: ScorecardNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardNoticeValidator(): ScorecardNoticeValidator {
    return ScorecardNoticeValidator(
        mutableListOf<ScorecardNoticeRule>(
            ScorecardNoticeRevisionRangeRule(32, 3600),
            ScorecardNoticeRequiredCodeRule("scorecard notice needs an entry"),
            ScorecardNoticeRequiredChecksumRule("scorecard notice needs a value"),
            ScorecardNoticeCodePrefixRule("X"),
        ),
    )
}
