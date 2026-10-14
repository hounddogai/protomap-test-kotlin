package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.text.normalize

/** A rule a weighted availability group must pass. Check returns its problems, or none. */
interface WeightedAvailabilityGroupRule {
    fun check(item: WeightedAvailabilityGroup): MutableList<String>
}

data class WeightedAvailabilityGroupMaximumRangeRule(
    var min: Double,
    var max: Double,
) : WeightedAvailabilityGroupRule {
    override fun check(item: WeightedAvailabilityGroup): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class WeightedAvailabilityGroupAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : WeightedAvailabilityGroupRule {
    override fun check(item: WeightedAvailabilityGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAvailabilityGroupCodeLengthRule(var maxLength: Int) : WeightedAvailabilityGroupRule {
    override fun check(item: WeightedAvailabilityGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAvailabilityGroupFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : WeightedAvailabilityGroupRule {
    override fun check(item: WeightedAvailabilityGroup): MutableList<String> {
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

data class WeightedAvailabilityGroupRequiredNameRule(var message: String) : WeightedAvailabilityGroupRule {
    override fun check(item: WeightedAvailabilityGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted availability group. */
data class WeightedAvailabilityGroupValidator(var rules: MutableList<WeightedAvailabilityGroupRule>) {
    fun validate(item: WeightedAvailabilityGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedAvailabilityGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAvailabilityGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAvailabilityGroupValidator(): WeightedAvailabilityGroupValidator {
    return WeightedAvailabilityGroupValidator(
        mutableListOf<WeightedAvailabilityGroupRule>(
            WeightedAvailabilityGroupMaximumRangeRule(0.5, 4502.5),
            WeightedAvailabilityGroupAllowedTrailerIdRule(mutableListOf<String>("pending")),
            WeightedAvailabilityGroupCodeLengthRule(255),
            WeightedAvailabilityGroupFuelLitersRangeRule(8.7, 454.6),
            WeightedAvailabilityGroupRequiredNameRule("weighted availability group needs this field"),
        ),
    )
}
