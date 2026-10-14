package com.acme.transport.emissions.emissionfactor.quota

/** A rule a emission factor entry must pass. Check returns its problems, or none. */
interface EmissionFactorEntryRule {
    fun check(item: EmissionFactorEntry): MutableList<String>
}

data class EmissionFactorEntryRequiredCodeRule(var message: String) : EmissionFactorEntryRule {
    override fun check(item: EmissionFactorEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorEntryCountryLengthRule(var maxLength: Int) : EmissionFactorEntryRule {
    override fun check(item: EmissionFactorEntry): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorEntryCodePrefixRule(var prefix: String) : EmissionFactorEntryRule {
    override fun check(item: EmissionFactorEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor entry. */
data class EmissionFactorEntryValidator(var rules: MutableList<EmissionFactorEntryRule>) {
    fun validate(item: EmissionFactorEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorEntryValidator(): EmissionFactorEntryValidator {
    return EmissionFactorEntryValidator(
        mutableListOf<EmissionFactorEntryRule>(
            EmissionFactorEntryRequiredCodeRule("emission factor entry needs a value"),
            EmissionFactorEntryCountryLengthRule(1000),
            EmissionFactorEntryCodePrefixRule("Q"),
        ),
    )
}
