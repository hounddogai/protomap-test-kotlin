package com.acme.transport.emissions.fuelburn.milestone

/** A rule a manual fuel burn threshold must pass. Check returns its problems, or none. */
interface ManualFuelBurnThresholdRule {
    fun check(item: ManualFuelBurnThreshold): MutableList<String>
}

data class ManualFuelBurnThresholdNameLengthRule(var maxLength: Int) : ManualFuelBurnThresholdRule {
    override fun check(item: ManualFuelBurnThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelBurnThresholdRevisionRangeRule(var min: Int, var max: Int) : ManualFuelBurnThresholdRule {
    override fun check(item: ManualFuelBurnThreshold): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class ManualFuelBurnThresholdMarginRangeRule(
    var min: Double,
    var max: Double,
) : ManualFuelBurnThresholdRule {
    override fun check(item: ManualFuelBurnThreshold): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelBurnThresholdDockLengthRule(var maxLength: Int) : ManualFuelBurnThresholdRule {
    override fun check(item: ManualFuelBurnThreshold): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual fuel burn threshold. */
data class ManualFuelBurnThresholdValidator(var rules: MutableList<ManualFuelBurnThresholdRule>) {
    fun validate(item: ManualFuelBurnThreshold): MutableList<String> {
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

    fun requireValid(item: ManualFuelBurnThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualFuelBurnThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultManualFuelBurnThresholdValidator(): ManualFuelBurnThresholdValidator {
    return ManualFuelBurnThresholdValidator(
        mutableListOf<ManualFuelBurnThresholdRule>(
            ManualFuelBurnThresholdNameLengthRule(16),
            ManualFuelBurnThresholdRevisionRangeRule(80, 86400),
            ManualFuelBurnThresholdMarginRangeRule(9.5, 1735.6),
            ManualFuelBurnThresholdDockLengthRule(64),
        ),
    )
}
