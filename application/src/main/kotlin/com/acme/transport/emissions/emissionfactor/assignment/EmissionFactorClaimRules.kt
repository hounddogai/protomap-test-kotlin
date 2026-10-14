package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.kit.text.normalize

/** A rule a emission factor claim must pass. Check returns its problems, or none. */
interface EmissionFactorClaimRule {
    fun check(item: EmissionFactorClaim): MutableList<String>
}

data class EmissionFactorClaimNameLengthRule(var maxLength: Int) : EmissionFactorClaimRule {
    override fun check(item: EmissionFactorClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorClaimAllowedRegionRule(var allowed: MutableList<String>) : EmissionFactorClaimRule {
    override fun check(item: EmissionFactorClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is too long")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorClaimCodePrefixRule(var prefix: String) : EmissionFactorClaimRule {
    override fun check(item: EmissionFactorClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorClaimRequiredCodeRule(var message: String) : EmissionFactorClaimRule {
    override fun check(item: EmissionFactorClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorClaimTimezoneLengthRule(var maxLength: Int) : EmissionFactorClaimRule {
    override fun check(item: EmissionFactorClaim): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor claim. */
data class EmissionFactorClaimValidator(var rules: MutableList<EmissionFactorClaimRule>) {
    fun validate(item: EmissionFactorClaim): MutableList<String> {
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

    fun requireValid(item: EmissionFactorClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorClaimValidator(): EmissionFactorClaimValidator {
    return EmissionFactorClaimValidator(
        mutableListOf<EmissionFactorClaimRule>(
            EmissionFactorClaimNameLengthRule(255),
            EmissionFactorClaimAllowedRegionRule(mutableListOf<String>("default", "adaptive", "batched")),
            EmissionFactorClaimCodePrefixRule("Q"),
            EmissionFactorClaimRequiredCodeRule("emission factor claim needs this field"),
            EmissionFactorClaimTimezoneLengthRule(120),
        ),
    )
}
