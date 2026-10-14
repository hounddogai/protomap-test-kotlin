package com.acme.transport.emissions.emissionfactor.schedule

/** A rule a emission factor segment must pass. Check returns its problems, or none. */
interface EmissionFactorSegmentRule {
    fun check(item: EmissionFactorSegment): MutableList<String>
}

data class EmissionFactorSegmentFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : EmissionFactorSegmentRule {
    override fun check(item: EmissionFactorSegment): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class EmissionFactorSegmentRequiredCodeRule(var message: String) : EmissionFactorSegmentRule {
    override fun check(item: EmissionFactorSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorSegmentNameLengthRule(var maxLength: Int) : EmissionFactorSegmentRule {
    override fun check(item: EmissionFactorSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor segment. */
data class EmissionFactorSegmentValidator(var rules: MutableList<EmissionFactorSegmentRule>) {
    fun validate(item: EmissionFactorSegment): MutableList<String> {
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

    fun requireValid(item: EmissionFactorSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorSegmentValidator(): EmissionFactorSegmentValidator {
    return EmissionFactorSegmentValidator(
        mutableListOf<EmissionFactorSegmentRule>(
            EmissionFactorSegmentFuelLitersRangeRule(9.6, 1091.9),
            EmissionFactorSegmentRequiredCodeRule("emission factor segment needs an entry"),
            EmissionFactorSegmentNameLengthRule(32),
        ),
    )
}
