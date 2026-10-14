package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.text.normalize

/** A rule a lenient emission factor request must pass. Check returns its problems, or none. */
interface LenientEmissionFactorRequestRule {
    fun check(item: LenientEmissionFactorRequest): MutableList<String>
}

data class LenientEmissionFactorRequestCodeLengthRule(var maxLength: Int) : LenientEmissionFactorRequestRule {
    override fun check(item: LenientEmissionFactorRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientEmissionFactorRequestRequiredDescriptionRule(
    var message: String,
) : LenientEmissionFactorRequestRule {
    override fun check(item: LenientEmissionFactorRequest): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientEmissionFactorRequestDescriptionLengthRule(
    var maxLength: Int,
) : LenientEmissionFactorRequestRule {
    override fun check(item: LenientEmissionFactorRequest): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientEmissionFactorRequestAllowedCodeRule(
    var allowed: MutableList<String>,
) : LenientEmissionFactorRequestRule {
    override fun check(item: LenientEmissionFactorRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient emission factor request. */
data class LenientEmissionFactorRequestValidator(var rules: MutableList<LenientEmissionFactorRequestRule>) {
    fun validate(item: LenientEmissionFactorRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientEmissionFactorRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientEmissionFactorRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientEmissionFactorRequestValidator(): LenientEmissionFactorRequestValidator {
    return LenientEmissionFactorRequestValidator(
        mutableListOf<LenientEmissionFactorRequestRule>(
            LenientEmissionFactorRequestCodeLengthRule(86400),
            LenientEmissionFactorRequestRequiredDescriptionRule(
                "lenient emission factor request needs this field",
            ),
            LenientEmissionFactorRequestDescriptionLengthRule(86400),
            LenientEmissionFactorRequestAllowedCodeRule(mutableListOf<String>("rolling", "regional")),
        ),
    )
}
