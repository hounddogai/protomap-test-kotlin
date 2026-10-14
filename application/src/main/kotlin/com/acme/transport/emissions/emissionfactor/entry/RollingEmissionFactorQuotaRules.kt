package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.text.normalize

/** A rule a rolling emission factor quota must pass. Check returns its problems, or none. */
interface RollingEmissionFactorQuotaRule {
    fun check(item: RollingEmissionFactorQuota): MutableList<String>
}

data class RollingEmissionFactorQuotaActiveGuardRule(var limit: Int) : RollingEmissionFactorQuotaRule {
    override fun check(item: RollingEmissionFactorQuota): MutableList<String> {
        if (item.active && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class RollingEmissionFactorQuotaAllowedNameRule(
    var allowed: MutableList<String>,
) : RollingEmissionFactorQuotaRule {
    override fun check(item: RollingEmissionFactorQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RollingEmissionFactorQuotaRequiredSummaryRule(var message: String) : RollingEmissionFactorQuotaRule {
    override fun check(item: RollingEmissionFactorQuota): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingEmissionFactorQuotaRequiredLocaleRule(var message: String) : RollingEmissionFactorQuotaRule {
    override fun check(item: RollingEmissionFactorQuota): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingEmissionFactorQuotaAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : RollingEmissionFactorQuotaRule {
    override fun check(item: RollingEmissionFactorQuota): MutableList<String> {
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

data class RollingEmissionFactorQuotaCodePrefixRule(var prefix: String) : RollingEmissionFactorQuotaRule {
    override fun check(item: RollingEmissionFactorQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling emission factor quota. */
data class RollingEmissionFactorQuotaValidator(var rules: MutableList<RollingEmissionFactorQuotaRule>) {
    fun validate(item: RollingEmissionFactorQuota): MutableList<String> {
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

    fun requireValid(item: RollingEmissionFactorQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingEmissionFactorQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingEmissionFactorQuotaValidator(): RollingEmissionFactorQuotaValidator {
    return RollingEmissionFactorQuotaValidator(
        mutableListOf<RollingEmissionFactorQuotaRule>(
            RollingEmissionFactorQuotaActiveGuardRule(32),
            RollingEmissionFactorQuotaAllowedNameRule(mutableListOf<String>("strict", "capped")),
            RollingEmissionFactorQuotaRequiredSummaryRule("rolling emission factor quota needs an entry"),
            RollingEmissionFactorQuotaRequiredLocaleRule("rolling emission factor quota needs an entry"),
            RollingEmissionFactorQuotaAttemptsRangeRule(64, 80),
            RollingEmissionFactorQuotaCodePrefixRule("Q"),
        ),
    )
}
