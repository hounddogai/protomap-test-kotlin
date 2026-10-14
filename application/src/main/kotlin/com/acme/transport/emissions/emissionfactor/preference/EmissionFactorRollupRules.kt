package com.acme.transport.emissions.emissionfactor.preference

/** A rule a emission factor rollup must pass. Check returns its problems, or none. */
interface EmissionFactorRollupRule {
    fun check(item: EmissionFactorRollup): MutableList<String>
}

data class EmissionFactorRollupThresholdRangeRule(var min: Double, var max: Double) : EmissionFactorRollupRule {
    override fun check(item: EmissionFactorRollup): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class EmissionFactorRollupTitleLengthRule(var maxLength: Int) : EmissionFactorRollupRule {
    override fun check(item: EmissionFactorRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorRollupShiftCodeLengthRule(var maxLength: Int) : EmissionFactorRollupRule {
    override fun check(item: EmissionFactorRollup): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorRollupRateRangeRule(var min: Double, var max: Double) : EmissionFactorRollupRule {
    override fun check(item: EmissionFactorRollup): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorRollupCodeLengthRule(var maxLength: Int) : EmissionFactorRollupRule {
    override fun check(item: EmissionFactorRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor rollup. */
data class EmissionFactorRollupValidator(var rules: MutableList<EmissionFactorRollupRule>) {
    fun validate(item: EmissionFactorRollup): MutableList<String> {
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

    fun requireValid(item: EmissionFactorRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorRollupValidator(): EmissionFactorRollupValidator {
    return EmissionFactorRollupValidator(
        mutableListOf<EmissionFactorRollupRule>(
            EmissionFactorRollupThresholdRangeRule(1.4, 2014.7),
            EmissionFactorRollupTitleLengthRule(16),
            EmissionFactorRollupShiftCodeLengthRule(80),
            EmissionFactorRollupRateRangeRule(3.9, 3084.7),
            EmissionFactorRollupCodeLengthRule(86400),
        ),
    )
}
