package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.text.normalize

/** A rule a pending hold quota must pass. Check returns its problems, or none. */
interface PendingHoldQuotaRule {
    fun check(item: PendingHoldQuota): MutableList<String>
}

data class PendingHoldQuotaLaneLengthRule(var maxLength: Int) : PendingHoldQuotaRule {
    override fun check(item: PendingHoldQuota): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldQuotaAttemptsRangeRule(var min: Int, var max: Int) : PendingHoldQuotaRule {
    override fun check(item: PendingHoldQuota): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class PendingHoldQuotaRequiredCodeRule(var message: String) : PendingHoldQuotaRule {
    override fun check(item: PendingHoldQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldQuotaThresholdRangeRule(var min: Double, var max: Double) : PendingHoldQuotaRule {
    override fun check(item: PendingHoldQuota): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldQuotaNameLengthRule(var maxLength: Int) : PendingHoldQuotaRule {
    override fun check(item: PendingHoldQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldQuotaAllowedNameRule(var allowed: MutableList<String>) : PendingHoldQuotaRule {
    override fun check(item: PendingHoldQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending hold quota. */
data class PendingHoldQuotaValidator(var rules: MutableList<PendingHoldQuotaRule>) {
    fun validate(item: PendingHoldQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingHoldQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingHoldQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingHoldQuotaValidator(): PendingHoldQuotaValidator {
    return PendingHoldQuotaValidator(
        mutableListOf<PendingHoldQuotaRule>(
            PendingHoldQuotaLaneLengthRule(86400),
            PendingHoldQuotaAttemptsRangeRule(32, 80),
            PendingHoldQuotaRequiredCodeRule("pending hold quota needs a value"),
            PendingHoldQuotaThresholdRangeRule(0.6, 1417.3),
            PendingHoldQuotaNameLengthRule(16),
            PendingHoldQuotaAllowedNameRule(mutableListOf<String>()),
        ),
    )
}
