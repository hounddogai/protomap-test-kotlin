package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.text.normalize

/** A rule a lenient lane plan agreement must pass. Check returns its problems, or none. */
interface LenientLanePlanAgreementRule {
    fun check(item: LenientLanePlanAgreement): MutableList<String>
}

data class LenientLanePlanAgreementCodePrefixRule(var prefix: String) : LenientLanePlanAgreementRule {
    override fun check(item: LenientLanePlanAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientLanePlanAgreementAllowedTitleRule(
    var allowed: MutableList<String>,
) : LenientLanePlanAgreementRule {
    override fun check(item: LenientLanePlanAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class LenientLanePlanAgreementCodeLengthRule(var maxLength: Int) : LenientLanePlanAgreementRule {
    override fun check(item: LenientLanePlanAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient lane plan agreement. */
data class LenientLanePlanAgreementValidator(var rules: MutableList<LenientLanePlanAgreementRule>) {
    fun validate(item: LenientLanePlanAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientLanePlanAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientLanePlanAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientLanePlanAgreementValidator(): LenientLanePlanAgreementValidator {
    return LenientLanePlanAgreementValidator(
        mutableListOf<LenientLanePlanAgreementRule>(
            LenientLanePlanAgreementCodePrefixRule("OPS"),
            LenientLanePlanAgreementAllowedTitleRule(mutableListOf<String>("fallback", "default")),
            LenientLanePlanAgreementCodeLengthRule(86400),
        ),
    )
}
