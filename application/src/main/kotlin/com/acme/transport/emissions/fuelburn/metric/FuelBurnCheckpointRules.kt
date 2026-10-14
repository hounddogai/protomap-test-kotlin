package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.kit.text.normalize

/** A rule a fuel burn checkpoint must pass. Check returns its problems, or none. */
interface FuelBurnCheckpointRule {
    fun check(item: FuelBurnCheckpoint): MutableList<String>
}

data class FuelBurnCheckpointNameLengthRule(var maxLength: Int) : FuelBurnCheckpointRule {
    override fun check(item: FuelBurnCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnCheckpointAllowedNameRule(var allowed: MutableList<String>) : FuelBurnCheckpointRule {
    override fun check(item: FuelBurnCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnCheckpointRequiredCategoryRule(var message: String) : FuelBurnCheckpointRule {
    override fun check(item: FuelBurnCheckpoint): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnCheckpointCategoryLengthRule(var maxLength: Int) : FuelBurnCheckpointRule {
    override fun check(item: FuelBurnCheckpoint): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnCheckpointPriorityRangeRule(var min: Int, var max: Int) : FuelBurnCheckpointRule {
    override fun check(item: FuelBurnCheckpoint): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn checkpoint. */
data class FuelBurnCheckpointValidator(var rules: MutableList<FuelBurnCheckpointRule>) {
    fun validate(item: FuelBurnCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnCheckpointValidator(): FuelBurnCheckpointValidator {
    return FuelBurnCheckpointValidator(
        mutableListOf<FuelBurnCheckpointRule>(
            FuelBurnCheckpointNameLengthRule(3600),
            FuelBurnCheckpointAllowedNameRule(mutableListOf<String>("batched")),
            FuelBurnCheckpointRequiredCategoryRule("fuel burn checkpoint needs an entry"),
            FuelBurnCheckpointCategoryLengthRule(86400),
            FuelBurnCheckpointPriorityRangeRule(120, 1000),
        ),
    )
}
