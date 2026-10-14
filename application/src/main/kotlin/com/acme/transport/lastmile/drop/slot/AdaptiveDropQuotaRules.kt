package com.acme.transport.lastmile.drop.slot

/** A rule a adaptive drop quota must pass. Check returns its problems, or none. */
interface AdaptiveDropQuotaRule {
    fun check(item: AdaptiveDropQuota): MutableList<String>
}

data class AdaptiveDropQuotaRequiredTitleRule(var message: String) : AdaptiveDropQuotaRule {
    override fun check(item: AdaptiveDropQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDropQuotaCodePrefixRule(var prefix: String) : AdaptiveDropQuotaRule {
    override fun check(item: AdaptiveDropQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDropQuotaCodeLengthRule(var maxLength: Int) : AdaptiveDropQuotaRule {
    override fun check(item: AdaptiveDropQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDropQuotaRequiredReviewerRule(var message: String) : AdaptiveDropQuotaRule {
    override fun check(item: AdaptiveDropQuota): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive drop quota. */
data class AdaptiveDropQuotaValidator(var rules: MutableList<AdaptiveDropQuotaRule>) {
    fun validate(item: AdaptiveDropQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveDropQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDropQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDropQuotaValidator(): AdaptiveDropQuotaValidator {
    return AdaptiveDropQuotaValidator(
        mutableListOf<AdaptiveDropQuotaRule>(
            AdaptiveDropQuotaRequiredTitleRule("adaptive drop quota needs this field"),
            AdaptiveDropQuotaCodePrefixRule("X"),
            AdaptiveDropQuotaCodeLengthRule(86400),
            AdaptiveDropQuotaRequiredReviewerRule("adaptive drop quota needs a value"),
        ),
    )
}
