package com.acme.transport.emissions.emissionfactor.factortarget

/** A rule a regional emission factor target must pass. Check returns its problems, or none. */
interface RegionalEmissionFactorTargetRule {
    fun check(item: RegionalEmissionFactorTarget): MutableList<String>
}

data class RegionalEmissionFactorTargetRequiredSummaryRule(
    var message: String,
) : RegionalEmissionFactorTargetRule {
    override fun check(item: RegionalEmissionFactorTarget): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalEmissionFactorTargetLabelLengthRule(var maxLength: Int) : RegionalEmissionFactorTargetRule {
    override fun check(item: RegionalEmissionFactorTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalEmissionFactorTargetThresholdRangeRule(
    var min: Double,
    var max: Double,
) : RegionalEmissionFactorTargetRule {
    override fun check(item: RegionalEmissionFactorTarget): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional emission factor target. */
data class RegionalEmissionFactorTargetValidator(var rules: MutableList<RegionalEmissionFactorTargetRule>) {
    fun validate(item: RegionalEmissionFactorTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalEmissionFactorTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalEmissionFactorTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalEmissionFactorTargetValidator(): RegionalEmissionFactorTargetValidator {
    return RegionalEmissionFactorTargetValidator(
        mutableListOf<RegionalEmissionFactorTargetRule>(
            RegionalEmissionFactorTargetRequiredSummaryRule("regional emission factor target needs a value"),
            RegionalEmissionFactorTargetLabelLengthRule(255),
            RegionalEmissionFactorTargetThresholdRangeRule(4.1, 1645.8),
        ),
    )
}
