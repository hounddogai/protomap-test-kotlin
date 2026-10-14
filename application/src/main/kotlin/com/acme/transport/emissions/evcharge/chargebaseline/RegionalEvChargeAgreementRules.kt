package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.kit.text.normalize

/** A rule a regional ev charge agreement must pass. Check returns its problems, or none. */
interface RegionalEvChargeAgreementRule {
    fun check(item: RegionalEvChargeAgreement): MutableList<String>
}

data class RegionalEvChargeAgreementRequiredLabelRule(var message: String) : RegionalEvChargeAgreementRule {
    override fun check(item: RegionalEvChargeAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalEvChargeAgreementRequiredCodeRule(var message: String) : RegionalEvChargeAgreementRule {
    override fun check(item: RegionalEvChargeAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalEvChargeAgreementAllowedCodeRule(
    var allowed: MutableList<String>,
) : RegionalEvChargeAgreementRule {
    override fun check(item: RegionalEvChargeAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional ev charge agreement. */
data class RegionalEvChargeAgreementValidator(var rules: MutableList<RegionalEvChargeAgreementRule>) {
    fun validate(item: RegionalEvChargeAgreement): MutableList<String> {
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

    fun requireValid(item: RegionalEvChargeAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalEvChargeAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalEvChargeAgreementValidator(): RegionalEvChargeAgreementValidator {
    return RegionalEvChargeAgreementValidator(
        mutableListOf<RegionalEvChargeAgreementRule>(
            RegionalEvChargeAgreementRequiredLabelRule("regional ev charge agreement needs a value"),
            RegionalEvChargeAgreementRequiredCodeRule("regional ev charge agreement needs a value"),
            RegionalEvChargeAgreementAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
