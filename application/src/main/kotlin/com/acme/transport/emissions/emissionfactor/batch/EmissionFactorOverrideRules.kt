package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.text.normalize

/** A rule a emission factor override must pass. Check returns its problems, or none. */
interface EmissionFactorOverrideRule {
    fun check(item: EmissionFactorOverride): MutableList<String>
}

data class EmissionFactorOverrideRequiredLocaleRule(var message: String) : EmissionFactorOverrideRule {
    override fun check(item: EmissionFactorOverride): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorOverrideAllowedChecksumRule(
    var allowed: MutableList<String>,
) : EmissionFactorOverrideRule {
    override fun check(item: EmissionFactorOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is too long")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorOverrideNameLengthRule(var maxLength: Int) : EmissionFactorOverrideRule {
    override fun check(item: EmissionFactorOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorOverrideChecksumLengthRule(var maxLength: Int) : EmissionFactorOverrideRule {
    override fun check(item: EmissionFactorOverride): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorOverrideRequiredCodeRule(var message: String) : EmissionFactorOverrideRule {
    override fun check(item: EmissionFactorOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor override. */
data class EmissionFactorOverrideValidator(var rules: MutableList<EmissionFactorOverrideRule>) {
    fun validate(item: EmissionFactorOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorOverrideValidator(): EmissionFactorOverrideValidator {
    return EmissionFactorOverrideValidator(
        mutableListOf<EmissionFactorOverrideRule>(
            EmissionFactorOverrideRequiredLocaleRule("emission factor override needs a value"),
            EmissionFactorOverrideAllowedChecksumRule(mutableListOf<String>("fallback")),
            EmissionFactorOverrideNameLengthRule(3600),
            EmissionFactorOverrideChecksumLengthRule(86400),
            EmissionFactorOverrideRequiredCodeRule("emission factor override needs a value"),
        ),
    )
}
