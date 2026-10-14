package com.acme.transport.emissions.emissionfactor.draft

/** A rule a emission factor variance must pass. Check returns its problems, or none. */
interface EmissionFactorVarianceRule {
    fun check(item: EmissionFactorVariance): MutableList<String>
}

data class EmissionFactorVarianceRequiredCodeRule(var message: String) : EmissionFactorVarianceRule {
    override fun check(item: EmissionFactorVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorVarianceCodePrefixRule(var prefix: String) : EmissionFactorVarianceRule {
    override fun check(item: EmissionFactorVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorVarianceDepotLengthRule(var maxLength: Int) : EmissionFactorVarianceRule {
    override fun check(item: EmissionFactorVariance): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorVarianceCodeLengthRule(var maxLength: Int) : EmissionFactorVarianceRule {
    override fun check(item: EmissionFactorVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor variance. */
data class EmissionFactorVarianceValidator(var rules: MutableList<EmissionFactorVarianceRule>) {
    fun validate(item: EmissionFactorVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorVarianceValidator(): EmissionFactorVarianceValidator {
    return EmissionFactorVarianceValidator(
        mutableListOf<EmissionFactorVarianceRule>(
            EmissionFactorVarianceRequiredCodeRule("emission factor variance needs an entry"),
            EmissionFactorVarianceCodePrefixRule("X"),
            EmissionFactorVarianceDepotLengthRule(16),
            EmissionFactorVarianceCodeLengthRule(64),
        ),
    )
}
