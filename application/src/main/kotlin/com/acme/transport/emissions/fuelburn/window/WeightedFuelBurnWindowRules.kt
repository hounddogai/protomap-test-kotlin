package com.acme.transport.emissions.fuelburn.window

/** A rule a weighted fuel burn window must pass. Check returns its problems, or none. */
interface WeightedFuelBurnWindowRule {
    fun check(item: WeightedFuelBurnWindow): MutableList<String>
}

data class WeightedFuelBurnWindowMarginRangeRule(
    var min: Double,
    var max: Double,
) : WeightedFuelBurnWindowRule {
    override fun check(item: WeightedFuelBurnWindow): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelBurnWindowCodePrefixRule(var prefix: String) : WeightedFuelBurnWindowRule {
    override fun check(item: WeightedFuelBurnWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelBurnWindowRequiredLabelRule(var message: String) : WeightedFuelBurnWindowRule {
    override fun check(item: WeightedFuelBurnWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelBurnWindowLabelLengthRule(var maxLength: Int) : WeightedFuelBurnWindowRule {
    override fun check(item: WeightedFuelBurnWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted fuel burn window. */
data class WeightedFuelBurnWindowValidator(var rules: MutableList<WeightedFuelBurnWindowRule>) {
    fun validate(item: WeightedFuelBurnWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedFuelBurnWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedFuelBurnWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedFuelBurnWindowValidator(): WeightedFuelBurnWindowValidator {
    return WeightedFuelBurnWindowValidator(
        mutableListOf<WeightedFuelBurnWindowRule>(
            WeightedFuelBurnWindowMarginRangeRule(8.1, 4308.6),
            WeightedFuelBurnWindowCodePrefixRule("REF"),
            WeightedFuelBurnWindowRequiredLabelRule("weighted fuel burn window needs this field"),
            WeightedFuelBurnWindowLabelLengthRule(120),
        ),
    )
}
