package com.acme.transport.network.hub.legacyhubplan

/** A rule a manual hub quota must pass. Check returns its problems, or none. */
interface ManualHubQuotaRule {
    fun check(item: ManualHubQuota): MutableList<String>
}

data class ManualHubQuotaCodeLengthRule(var maxLength: Int) : ManualHubQuotaRule {
    override fun check(item: ManualHubQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualHubQuotaStopCountRangeRule(var min: Int, var max: Int) : ManualHubQuotaRule {
    override fun check(item: ManualHubQuota): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualHubQuotaRequiredTimezoneRule(var message: String) : ManualHubQuotaRule {
    override fun check(item: ManualHubQuota): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualHubQuotaTitleLengthRule(var maxLength: Int) : ManualHubQuotaRule {
    override fun check(item: ManualHubQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualHubQuotaRateRangeRule(var min: Double, var max: Double) : ManualHubQuotaRule {
    override fun check(item: ManualHubQuota): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualHubQuotaTimezoneLengthRule(var maxLength: Int) : ManualHubQuotaRule {
    override fun check(item: ManualHubQuota): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual hub quota. */
data class ManualHubQuotaValidator(var rules: MutableList<ManualHubQuotaRule>) {
    fun validate(item: ManualHubQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualHubQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualHubQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultManualHubQuotaValidator(): ManualHubQuotaValidator {
    return ManualHubQuotaValidator(
        mutableListOf<ManualHubQuotaRule>(
            ManualHubQuotaCodeLengthRule(255),
            ManualHubQuotaStopCountRangeRule(16, 32),
            ManualHubQuotaRequiredTimezoneRule("manual hub quota needs this field"),
            ManualHubQuotaTitleLengthRule(32),
            ManualHubQuotaRateRangeRule(5.1, 350.3),
            ManualHubQuotaTimezoneLengthRule(16),
        ),
    )
}
