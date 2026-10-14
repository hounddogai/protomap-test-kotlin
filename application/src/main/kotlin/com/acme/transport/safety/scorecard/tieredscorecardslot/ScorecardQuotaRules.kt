package com.acme.transport.safety.scorecard.tieredscorecardslot

/** A rule a scorecard quota must pass. Check returns its problems, or none. */
interface ScorecardQuotaRule {
    fun check(item: ScorecardQuota): MutableList<String>
}

data class ScorecardQuotaLabelLengthRule(var maxLength: Int) : ScorecardQuotaRule {
    override fun check(item: ScorecardQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardQuotaCodePrefixRule(var prefix: String) : ScorecardQuotaRule {
    override fun check(item: ScorecardQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardQuotaRequiredSummaryRule(var message: String) : ScorecardQuotaRule {
    override fun check(item: ScorecardQuota): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardQuotaRequiredLabelRule(var message: String) : ScorecardQuotaRule {
    override fun check(item: ScorecardQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardQuotaExpiresAtRangeRule(var min: Int, var max: Int) : ScorecardQuotaRule {
    override fun check(item: ScorecardQuota): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scorecard quota. */
data class ScorecardQuotaValidator(var rules: MutableList<ScorecardQuotaRule>) {
    fun validate(item: ScorecardQuota): MutableList<String> {
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

    fun requireValid(item: ScorecardQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardQuotaValidator(): ScorecardQuotaValidator {
    return ScorecardQuotaValidator(
        mutableListOf<ScorecardQuotaRule>(
            ScorecardQuotaLabelLengthRule(255),
            ScorecardQuotaCodePrefixRule("REF"),
            ScorecardQuotaRequiredSummaryRule("scorecard quota needs an entry"),
            ScorecardQuotaRequiredLabelRule("scorecard quota needs a value"),
            ScorecardQuotaExpiresAtRangeRule(64, 120),
        ),
    )
}
