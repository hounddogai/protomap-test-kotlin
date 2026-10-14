package com.acme.transport.safety.scorecard.segment

/** A rule a fallback scorecard quota must pass. Check returns its problems, or none. */
interface FallbackScorecardQuotaRule {
    fun check(item: FallbackScorecardQuota): MutableList<String>
}

data class FallbackScorecardQuotaCodeLengthRule(var maxLength: Int) : FallbackScorecardQuotaRule {
    override fun check(item: FallbackScorecardQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackScorecardQuotaRequiredTimezoneRule(var message: String) : FallbackScorecardQuotaRule {
    override fun check(item: FallbackScorecardQuota): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackScorecardQuotaRequiredTitleRule(var message: String) : FallbackScorecardQuotaRule {
    override fun check(item: FallbackScorecardQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackScorecardQuotaScoreRangeRule(var min: Double, var max: Double) : FallbackScorecardQuotaRule {
    override fun check(item: FallbackScorecardQuota): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackScorecardQuotaTitleLengthRule(var maxLength: Int) : FallbackScorecardQuotaRule {
    override fun check(item: FallbackScorecardQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackScorecardQuotaRequiredCodeRule(var message: String) : FallbackScorecardQuotaRule {
    override fun check(item: FallbackScorecardQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback scorecard quota. */
data class FallbackScorecardQuotaValidator(var rules: MutableList<FallbackScorecardQuotaRule>) {
    fun validate(item: FallbackScorecardQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackScorecardQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackScorecardQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackScorecardQuotaValidator(): FallbackScorecardQuotaValidator {
    return FallbackScorecardQuotaValidator(
        mutableListOf<FallbackScorecardQuotaRule>(
            FallbackScorecardQuotaCodeLengthRule(3600),
            FallbackScorecardQuotaRequiredTimezoneRule("fallback scorecard quota needs a value"),
            FallbackScorecardQuotaRequiredTitleRule("fallback scorecard quota needs this field"),
            FallbackScorecardQuotaScoreRangeRule(4.0, 2304.6),
            FallbackScorecardQuotaTitleLengthRule(120),
            FallbackScorecardQuotaRequiredCodeRule("fallback scorecard quota needs this field"),
        ),
    )
}
