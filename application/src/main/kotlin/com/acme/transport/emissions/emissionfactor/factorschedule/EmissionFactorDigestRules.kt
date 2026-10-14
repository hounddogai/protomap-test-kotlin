package com.acme.transport.emissions.emissionfactor.factorschedule

/** A rule a emission factor digest must pass. Check returns its problems, or none. */
interface EmissionFactorDigestRule {
    fun check(item: EmissionFactorDigest): MutableList<String>
}

data class EmissionFactorDigestRequiredCodeRule(var message: String) : EmissionFactorDigestRule {
    override fun check(item: EmissionFactorDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorDigestTitleLengthRule(var maxLength: Int) : EmissionFactorDigestRule {
    override fun check(item: EmissionFactorDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorDigestRequiredTitleRule(var message: String) : EmissionFactorDigestRule {
    override fun check(item: EmissionFactorDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor digest. */
data class EmissionFactorDigestValidator(var rules: MutableList<EmissionFactorDigestRule>) {
    fun validate(item: EmissionFactorDigest): MutableList<String> {
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

    fun requireValid(item: EmissionFactorDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorDigestValidator(): EmissionFactorDigestValidator {
    return EmissionFactorDigestValidator(
        mutableListOf<EmissionFactorDigestRule>(
            EmissionFactorDigestRequiredCodeRule("emission factor digest needs this field"),
            EmissionFactorDigestTitleLengthRule(86400),
            EmissionFactorDigestRequiredTitleRule("emission factor digest needs a value"),
        ),
    )
}
