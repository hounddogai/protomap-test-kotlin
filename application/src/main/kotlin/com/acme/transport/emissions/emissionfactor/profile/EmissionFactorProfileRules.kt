package com.acme.transport.emissions.emissionfactor.profile

/** A rule a emission factor profile must pass. Check returns its problems, or none. */
interface EmissionFactorProfileRule {
    fun check(item: EmissionFactorProfile): MutableList<String>
}

data class EmissionFactorProfileUnitLengthRule(var maxLength: Int) : EmissionFactorProfileRule {
    override fun check(item: EmissionFactorProfile): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorProfileMaximumRangeRule(var min: Double, var max: Double) : EmissionFactorProfileRule {
    override fun check(item: EmissionFactorProfile): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorProfileCodeLengthRule(var maxLength: Int) : EmissionFactorProfileRule {
    override fun check(item: EmissionFactorProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor profile. */
data class EmissionFactorProfileValidator(var rules: MutableList<EmissionFactorProfileRule>) {
    fun validate(item: EmissionFactorProfile): MutableList<String> {
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

    fun requireValid(item: EmissionFactorProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorProfileValidator(): EmissionFactorProfileValidator {
    return EmissionFactorProfileValidator(
        mutableListOf<EmissionFactorProfileRule>(
            EmissionFactorProfileUnitLengthRule(86400),
            EmissionFactorProfileMaximumRangeRule(6.9, 3178.2),
            EmissionFactorProfileCodeLengthRule(1000),
        ),
    )
}
