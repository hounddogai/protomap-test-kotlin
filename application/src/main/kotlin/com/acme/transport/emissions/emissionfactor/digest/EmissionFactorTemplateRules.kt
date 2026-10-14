package com.acme.transport.emissions.emissionfactor.digest

/** A rule a emission factor template must pass. Check returns its problems, or none. */
interface EmissionFactorTemplateRule {
    fun check(item: EmissionFactorTemplate): MutableList<String>
}

data class EmissionFactorTemplateEndsAtRangeRule(var min: Int, var max: Int) : EmissionFactorTemplateRule {
    override fun check(item: EmissionFactorTemplate): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class EmissionFactorTemplateVehicleIdLengthRule(var maxLength: Int) : EmissionFactorTemplateRule {
    override fun check(item: EmissionFactorTemplate): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorTemplateThresholdRangeRule(
    var min: Double,
    var max: Double,
) : EmissionFactorTemplateRule {
    override fun check(item: EmissionFactorTemplate): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor template. */
data class EmissionFactorTemplateValidator(var rules: MutableList<EmissionFactorTemplateRule>) {
    fun validate(item: EmissionFactorTemplate): MutableList<String> {
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

    fun requireValid(item: EmissionFactorTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorTemplateValidator(): EmissionFactorTemplateValidator {
    return EmissionFactorTemplateValidator(
        mutableListOf<EmissionFactorTemplateRule>(
            EmissionFactorTemplateEndsAtRangeRule(16, 255),
            EmissionFactorTemplateVehicleIdLengthRule(120),
            EmissionFactorTemplateThresholdRangeRule(9.3, 1785.5),
        ),
    )
}
