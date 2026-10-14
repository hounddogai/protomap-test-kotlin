package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.text.normalize

/** A rule a manual attempt quota must pass. Check returns its problems, or none. */
interface ManualAttemptQuotaRule {
    fun check(item: ManualAttemptQuota): MutableList<String>
}

data class ManualAttemptQuotaTitleLengthRule(var maxLength: Int) : ManualAttemptQuotaRule {
    override fun check(item: ManualAttemptQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptQuotaRequiredSegmentRule(var message: String) : ManualAttemptQuotaRule {
    override fun check(item: ManualAttemptQuota): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptQuotaAllowedCodeRule(var allowed: MutableList<String>) : ManualAttemptQuotaRule {
    override fun check(item: ManualAttemptQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptQuotaRequiredOwnerIdRule(var message: String) : ManualAttemptQuotaRule {
    override fun check(item: ManualAttemptQuota): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptQuotaThresholdRangeRule(var min: Double, var max: Double) : ManualAttemptQuotaRule {
    override fun check(item: ManualAttemptQuota): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual attempt quota. */
data class ManualAttemptQuotaValidator(var rules: MutableList<ManualAttemptQuotaRule>) {
    fun validate(item: ManualAttemptQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualAttemptQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAttemptQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAttemptQuotaValidator(): ManualAttemptQuotaValidator {
    return ManualAttemptQuotaValidator(
        mutableListOf<ManualAttemptQuotaRule>(
            ManualAttemptQuotaTitleLengthRule(80),
            ManualAttemptQuotaRequiredSegmentRule("manual attempt quota needs an entry"),
            ManualAttemptQuotaAllowedCodeRule(mutableListOf<String>("capped", "capped", "regional")),
            ManualAttemptQuotaRequiredOwnerIdRule("manual attempt quota needs a value"),
            ManualAttemptQuotaThresholdRangeRule(0.8, 2555.1),
        ),
    )
}
