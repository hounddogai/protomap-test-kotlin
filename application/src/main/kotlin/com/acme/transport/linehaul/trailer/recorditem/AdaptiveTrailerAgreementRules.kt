package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a adaptive trailer agreement must pass. Check returns its problems, or none. */
interface AdaptiveTrailerAgreementRule {
    fun check(item: AdaptiveTrailerAgreement): MutableList<String>
}

data class AdaptiveTrailerAgreementRequiredCodeRule(var message: String) : AdaptiveTrailerAgreementRule {
    override fun check(item: AdaptiveTrailerAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTrailerAgreementAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveTrailerAgreementRule {
    override fun check(item: AdaptiveTrailerAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTrailerAgreementCodePrefixRule(var prefix: String) : AdaptiveTrailerAgreementRule {
    override fun check(item: AdaptiveTrailerAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTrailerAgreementRequiredVehicleIdRule(var message: String) : AdaptiveTrailerAgreementRule {
    override fun check(item: AdaptiveTrailerAgreement): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive trailer agreement. */
data class AdaptiveTrailerAgreementValidator(var rules: MutableList<AdaptiveTrailerAgreementRule>) {
    fun validate(item: AdaptiveTrailerAgreement): MutableList<String> {
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

    fun requireValid(item: AdaptiveTrailerAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTrailerAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTrailerAgreementValidator(): AdaptiveTrailerAgreementValidator {
    return AdaptiveTrailerAgreementValidator(
        mutableListOf<AdaptiveTrailerAgreementRule>(
            AdaptiveTrailerAgreementRequiredCodeRule("adaptive trailer agreement needs this field"),
            AdaptiveTrailerAgreementAllowedCodeRule(mutableListOf<String>("regional", "fallback")),
            AdaptiveTrailerAgreementCodePrefixRule("X"),
            AdaptiveTrailerAgreementRequiredVehicleIdRule("adaptive trailer agreement needs this field"),
        ),
    )
}
