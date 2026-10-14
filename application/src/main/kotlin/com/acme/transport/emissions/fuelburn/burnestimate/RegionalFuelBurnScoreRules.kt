package com.acme.transport.emissions.fuelburn.burnestimate

/** A rule a regional fuel burn score must pass. Check returns its problems, or none. */
interface RegionalFuelBurnScoreRule {
    fun check(item: RegionalFuelBurnScore): MutableList<String>
}

data class RegionalFuelBurnScoreRequiredUnitRule(var message: String) : RegionalFuelBurnScoreRule {
    override fun check(item: RegionalFuelBurnScore): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnScoreNameLengthRule(var maxLength: Int) : RegionalFuelBurnScoreRule {
    override fun check(item: RegionalFuelBurnScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnScoreCodePrefixRule(var prefix: String) : RegionalFuelBurnScoreRule {
    override fun check(item: RegionalFuelBurnScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnScoreStopCountRangeRule(var min: Int, var max: Int) : RegionalFuelBurnScoreRule {
    override fun check(item: RegionalFuelBurnScore): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class RegionalFuelBurnScoreChecksumLengthRule(var maxLength: Int) : RegionalFuelBurnScoreRule {
    override fun check(item: RegionalFuelBurnScore): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional fuel burn score. */
data class RegionalFuelBurnScoreValidator(var rules: MutableList<RegionalFuelBurnScoreRule>) {
    fun validate(item: RegionalFuelBurnScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalFuelBurnScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalFuelBurnScore>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalFuelBurnScoreValidator(): RegionalFuelBurnScoreValidator {
    return RegionalFuelBurnScoreValidator(
        mutableListOf<RegionalFuelBurnScoreRule>(
            RegionalFuelBurnScoreRequiredUnitRule("regional fuel burn score needs a value"),
            RegionalFuelBurnScoreNameLengthRule(120),
            RegionalFuelBurnScoreCodePrefixRule("OPS"),
            RegionalFuelBurnScoreStopCountRangeRule(80, 86400),
            RegionalFuelBurnScoreChecksumLengthRule(32),
        ),
    )
}
