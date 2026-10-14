package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.text.normalize

/** A rule a seasonal service area quota must pass. Check returns its problems, or none. */
interface SeasonalServiceAreaQuotaRule {
    fun check(item: SeasonalServiceAreaQuota): MutableList<String>
}

data class SeasonalServiceAreaQuotaRequiredExternalRefRule(var message: String) : SeasonalServiceAreaQuotaRule {
    override fun check(item: SeasonalServiceAreaQuota): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServiceAreaQuotaAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : SeasonalServiceAreaQuotaRule {
    override fun check(item: SeasonalServiceAreaQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is expired")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServiceAreaQuotaVersionRangeRule(var min: Int, var max: Int) : SeasonalServiceAreaQuotaRule {
    override fun check(item: SeasonalServiceAreaQuota): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServiceAreaQuotaCodePrefixRule(var prefix: String) : SeasonalServiceAreaQuotaRule {
    override fun check(item: SeasonalServiceAreaQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal service area quota. */
data class SeasonalServiceAreaQuotaValidator(var rules: MutableList<SeasonalServiceAreaQuotaRule>) {
    fun validate(item: SeasonalServiceAreaQuota): MutableList<String> {
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

    fun requireValid(item: SeasonalServiceAreaQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalServiceAreaQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalServiceAreaQuotaValidator(): SeasonalServiceAreaQuotaValidator {
    return SeasonalServiceAreaQuotaValidator(
        mutableListOf<SeasonalServiceAreaQuotaRule>(
            SeasonalServiceAreaQuotaRequiredExternalRefRule("seasonal service area quota needs a value"),
            SeasonalServiceAreaQuotaAllowedExternalRefRule(mutableListOf<String>("manual", "capped")),
            SeasonalServiceAreaQuotaVersionRangeRule(80, 86400),
            SeasonalServiceAreaQuotaCodePrefixRule("OPS"),
        ),
    )
}
