package com.acme.transport.emissions.emissionfactor.agreement

import com.acme.transport.kit.text.normalize

/** A rule a seasonal emission factor quota must pass. Check returns its problems, or none. */
interface SeasonalEmissionFactorQuotaRule {
    fun check(item: SeasonalEmissionFactorQuota): MutableList<String>
}

data class SeasonalEmissionFactorQuotaRequiredCountryRule(
    var message: String,
) : SeasonalEmissionFactorQuotaRule {
    override fun check(item: SeasonalEmissionFactorQuota): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorQuotaCodeLengthRule(var maxLength: Int) : SeasonalEmissionFactorQuotaRule {
    override fun check(item: SeasonalEmissionFactorQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorQuotaAllowedNameRule(
    var allowed: MutableList<String>,
) : SeasonalEmissionFactorQuotaRule {
    override fun check(item: SeasonalEmissionFactorQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorQuotaRequiredTimezoneRule(
    var message: String,
) : SeasonalEmissionFactorQuotaRule {
    override fun check(item: SeasonalEmissionFactorQuota): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal emission factor quota. */
data class SeasonalEmissionFactorQuotaValidator(var rules: MutableList<SeasonalEmissionFactorQuotaRule>) {
    fun validate(item: SeasonalEmissionFactorQuota): MutableList<String> {
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

    fun requireValid(item: SeasonalEmissionFactorQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalEmissionFactorQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalEmissionFactorQuotaValidator(): SeasonalEmissionFactorQuotaValidator {
    return SeasonalEmissionFactorQuotaValidator(
        mutableListOf<SeasonalEmissionFactorQuotaRule>(
            SeasonalEmissionFactorQuotaRequiredCountryRule("seasonal emission factor quota needs a value"),
            SeasonalEmissionFactorQuotaCodeLengthRule(32),
            SeasonalEmissionFactorQuotaAllowedNameRule(mutableListOf<String>("fallback", "capped", "adaptive")),
            SeasonalEmissionFactorQuotaRequiredTimezoneRule("seasonal emission factor quota needs an entry"),
        ),
    )
}
