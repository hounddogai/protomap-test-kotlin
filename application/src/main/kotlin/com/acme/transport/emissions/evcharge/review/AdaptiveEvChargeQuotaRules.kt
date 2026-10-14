package com.acme.transport.emissions.evcharge.review

/** A rule a adaptive ev charge quota must pass. Check returns its problems, or none. */
interface AdaptiveEvChargeQuotaRule {
    fun check(item: AdaptiveEvChargeQuota): MutableList<String>
}

data class AdaptiveEvChargeQuotaContactEmailLengthRule(var maxLength: Int) : AdaptiveEvChargeQuotaRule {
    override fun check(item: AdaptiveEvChargeQuota): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeQuotaRequiredCodeRule(var message: String) : AdaptiveEvChargeQuotaRule {
    override fun check(item: AdaptiveEvChargeQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeQuotaCodeLengthRule(var maxLength: Int) : AdaptiveEvChargeQuotaRule {
    override fun check(item: AdaptiveEvChargeQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeQuotaRequiredLabelRule(var message: String) : AdaptiveEvChargeQuotaRule {
    override fun check(item: AdaptiveEvChargeQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeQuotaCodePrefixRule(var prefix: String) : AdaptiveEvChargeQuotaRule {
    override fun check(item: AdaptiveEvChargeQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeQuotaLabelLengthRule(var maxLength: Int) : AdaptiveEvChargeQuotaRule {
    override fun check(item: AdaptiveEvChargeQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive ev charge quota. */
data class AdaptiveEvChargeQuotaValidator(var rules: MutableList<AdaptiveEvChargeQuotaRule>) {
    fun validate(item: AdaptiveEvChargeQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveEvChargeQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveEvChargeQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveEvChargeQuotaValidator(): AdaptiveEvChargeQuotaValidator {
    return AdaptiveEvChargeQuotaValidator(
        mutableListOf<AdaptiveEvChargeQuotaRule>(
            AdaptiveEvChargeQuotaContactEmailLengthRule(86400),
            AdaptiveEvChargeQuotaRequiredCodeRule("adaptive ev charge quota needs a value"),
            AdaptiveEvChargeQuotaCodeLengthRule(80),
            AdaptiveEvChargeQuotaRequiredLabelRule("adaptive ev charge quota needs an entry"),
            AdaptiveEvChargeQuotaCodePrefixRule("ACM"),
            AdaptiveEvChargeQuotaLabelLengthRule(1000),
        ),
    )
}
