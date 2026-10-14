package com.acme.transport.emissions.fuelburn.audit

/** A rule a weighted fuel burn target must pass. Check returns its problems, or none. */
interface WeightedFuelBurnTargetRule {
    fun check(item: WeightedFuelBurnTarget): MutableList<String>
}

data class WeightedFuelBurnTargetCeilingRangeRule(
    var min: Double,
    var max: Double,
) : WeightedFuelBurnTargetRule {
    override fun check(item: WeightedFuelBurnTarget): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelBurnTargetOdometerRangeRule(var min: Int, var max: Int) : WeightedFuelBurnTargetRule {
    override fun check(item: WeightedFuelBurnTarget): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelBurnTargetNameLengthRule(var maxLength: Int) : WeightedFuelBurnTargetRule {
    override fun check(item: WeightedFuelBurnTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted fuel burn target. */
data class WeightedFuelBurnTargetValidator(var rules: MutableList<WeightedFuelBurnTargetRule>) {
    fun validate(item: WeightedFuelBurnTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedFuelBurnTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedFuelBurnTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedFuelBurnTargetValidator(): WeightedFuelBurnTargetValidator {
    return WeightedFuelBurnTargetValidator(
        mutableListOf<WeightedFuelBurnTargetRule>(
            WeightedFuelBurnTargetCeilingRangeRule(1.6, 491.8),
            WeightedFuelBurnTargetOdometerRangeRule(120, 3600),
            WeightedFuelBurnTargetNameLengthRule(64),
        ),
    )
}
