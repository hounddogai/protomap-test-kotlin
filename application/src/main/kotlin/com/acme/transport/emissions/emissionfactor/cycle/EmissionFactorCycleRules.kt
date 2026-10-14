package com.acme.transport.emissions.emissionfactor.cycle

/** A rule a emission factor cycle must pass. Check returns its problems, or none. */
interface EmissionFactorCycleRule {
    fun check(item: EmissionFactorCycle): MutableList<String>
}

data class EmissionFactorCycleRequiredCodeRule(var message: String) : EmissionFactorCycleRule {
    override fun check(item: EmissionFactorCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorCycleCodePrefixRule(var prefix: String) : EmissionFactorCycleRule {
    override fun check(item: EmissionFactorCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorCycleUpdatedAtRangeRule(var min: Int, var max: Int) : EmissionFactorCycleRule {
    override fun check(item: EmissionFactorCycle): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorCycleNameLengthRule(var maxLength: Int) : EmissionFactorCycleRule {
    override fun check(item: EmissionFactorCycle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor cycle. */
data class EmissionFactorCycleValidator(var rules: MutableList<EmissionFactorCycleRule>) {
    fun validate(item: EmissionFactorCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorCycleValidator(): EmissionFactorCycleValidator {
    return EmissionFactorCycleValidator(
        mutableListOf<EmissionFactorCycleRule>(
            EmissionFactorCycleRequiredCodeRule("emission factor cycle needs a value"),
            EmissionFactorCycleCodePrefixRule("Q"),
            EmissionFactorCycleUpdatedAtRangeRule(32, 80),
            EmissionFactorCycleNameLengthRule(16),
        ),
    )
}
