package com.acme.transport.emissions.fuelburn.adjustment

/** A rule a regional fuel burn target must pass. Check returns its problems, or none. */
interface RegionalFuelBurnTargetRule {
    fun check(item: RegionalFuelBurnTarget): MutableList<String>
}

data class RegionalFuelBurnTargetWindowDaysRangeRule(var min: Int, var max: Int) : RegionalFuelBurnTargetRule {
    override fun check(item: RegionalFuelBurnTarget): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnTargetRequiredCountryRule(var message: String) : RegionalFuelBurnTargetRule {
    override fun check(item: RegionalFuelBurnTarget): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnTargetTitleLengthRule(var maxLength: Int) : RegionalFuelBurnTargetRule {
    override fun check(item: RegionalFuelBurnTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnTargetVersionRangeRule(var min: Int, var max: Int) : RegionalFuelBurnTargetRule {
    override fun check(item: RegionalFuelBurnTarget): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional fuel burn target. */
data class RegionalFuelBurnTargetValidator(var rules: MutableList<RegionalFuelBurnTargetRule>) {
    fun validate(item: RegionalFuelBurnTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalFuelBurnTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalFuelBurnTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalFuelBurnTargetValidator(): RegionalFuelBurnTargetValidator {
    return RegionalFuelBurnTargetValidator(
        mutableListOf<RegionalFuelBurnTargetRule>(
            RegionalFuelBurnTargetWindowDaysRangeRule(3600, 3600),
            RegionalFuelBurnTargetRequiredCountryRule("regional fuel burn target needs this field"),
            RegionalFuelBurnTargetTitleLengthRule(86400),
            RegionalFuelBurnTargetVersionRangeRule(255, 86400),
        ),
    )
}
