package com.acme.transport.emissions.fuelburn.ledger

/** A rule a weighted fuel burn mapping must pass. Check returns its problems, or none. */
interface WeightedFuelBurnMappingRule {
    fun check(item: WeightedFuelBurnMapping): MutableList<String>
}

data class WeightedFuelBurnMappingLabelLengthRule(var maxLength: Int) : WeightedFuelBurnMappingRule {
    override fun check(item: WeightedFuelBurnMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelBurnMappingRequiredLabelRule(var message: String) : WeightedFuelBurnMappingRule {
    override fun check(item: WeightedFuelBurnMapping): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelBurnMappingLaneLengthRule(var maxLength: Int) : WeightedFuelBurnMappingRule {
    override fun check(item: WeightedFuelBurnMapping): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelBurnMappingPriorityRangeRule(var min: Int, var max: Int) : WeightedFuelBurnMappingRule {
    override fun check(item: WeightedFuelBurnMapping): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class WeightedFuelBurnMappingRequiredReviewerRule(var message: String) : WeightedFuelBurnMappingRule {
    override fun check(item: WeightedFuelBurnMapping): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted fuel burn mapping. */
data class WeightedFuelBurnMappingValidator(var rules: MutableList<WeightedFuelBurnMappingRule>) {
    fun validate(item: WeightedFuelBurnMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedFuelBurnMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedFuelBurnMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedFuelBurnMappingValidator(): WeightedFuelBurnMappingValidator {
    return WeightedFuelBurnMappingValidator(
        mutableListOf<WeightedFuelBurnMappingRule>(
            WeightedFuelBurnMappingLabelLengthRule(120),
            WeightedFuelBurnMappingRequiredLabelRule("weighted fuel burn mapping needs this field"),
            WeightedFuelBurnMappingLaneLengthRule(16),
            WeightedFuelBurnMappingPriorityRangeRule(86400, 86400),
            WeightedFuelBurnMappingRequiredReviewerRule("weighted fuel burn mapping needs this field"),
        ),
    )
}
