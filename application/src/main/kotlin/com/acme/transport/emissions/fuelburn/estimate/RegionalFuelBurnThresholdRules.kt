package com.acme.transport.emissions.fuelburn.estimate

/** A rule a regional fuel burn threshold must pass. Check returns its problems, or none. */
interface RegionalFuelBurnThresholdRule {
    fun check(item: RegionalFuelBurnThreshold): MutableList<String>
}

data class RegionalFuelBurnThresholdRequiredCodeRule(var message: String) : RegionalFuelBurnThresholdRule {
    override fun check(item: RegionalFuelBurnThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnThresholdLabelLengthRule(var maxLength: Int) : RegionalFuelBurnThresholdRule {
    override fun check(item: RegionalFuelBurnThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnThresholdCodePrefixRule(var prefix: String) : RegionalFuelBurnThresholdRule {
    override fun check(item: RegionalFuelBurnThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnThresholdLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : RegionalFuelBurnThresholdRule {
    override fun check(item: RegionalFuelBurnThreshold): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnThresholdCapacityRangeRule(
    var min: Int,
    var max: Int,
) : RegionalFuelBurnThresholdRule {
    override fun check(item: RegionalFuelBurnThreshold): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnThresholdCodeLengthRule(var maxLength: Int) : RegionalFuelBurnThresholdRule {
    override fun check(item: RegionalFuelBurnThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional fuel burn threshold. */
data class RegionalFuelBurnThresholdValidator(var rules: MutableList<RegionalFuelBurnThresholdRule>) {
    fun validate(item: RegionalFuelBurnThreshold): MutableList<String> {
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

    fun requireValid(item: RegionalFuelBurnThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalFuelBurnThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalFuelBurnThresholdValidator(): RegionalFuelBurnThresholdValidator {
    return RegionalFuelBurnThresholdValidator(
        mutableListOf<RegionalFuelBurnThresholdRule>(
            RegionalFuelBurnThresholdRequiredCodeRule("regional fuel burn threshold needs an entry"),
            RegionalFuelBurnThresholdLabelLengthRule(16),
            RegionalFuelBurnThresholdCodePrefixRule("ACM"),
            RegionalFuelBurnThresholdLoadKgRangeRule(7.5, 2766.2),
            RegionalFuelBurnThresholdCapacityRangeRule(16, 120),
            RegionalFuelBurnThresholdCodeLengthRule(32),
        ),
    )
}
