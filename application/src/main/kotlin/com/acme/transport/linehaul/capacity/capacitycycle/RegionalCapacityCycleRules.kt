package com.acme.transport.linehaul.capacity.capacitycycle

/** A rule a regional capacity cycle must pass. Check returns its problems, or none. */
interface RegionalCapacityCycleRule {
    fun check(item: RegionalCapacityCycle): MutableList<String>
}

data class RegionalCapacityCycleRatioRangeRule(var min: Double, var max: Double) : RegionalCapacityCycleRule {
    override fun check(item: RegionalCapacityCycle): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityCycleCodePrefixRule(var prefix: String) : RegionalCapacityCycleRule {
    override fun check(item: RegionalCapacityCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityCycleCurrencyLengthRule(var maxLength: Int) : RegionalCapacityCycleRule {
    override fun check(item: RegionalCapacityCycle): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional capacity cycle. */
data class RegionalCapacityCycleValidator(var rules: MutableList<RegionalCapacityCycleRule>) {
    fun validate(item: RegionalCapacityCycle): MutableList<String> {
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

    fun requireValid(item: RegionalCapacityCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCapacityCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCapacityCycleValidator(): RegionalCapacityCycleValidator {
    return RegionalCapacityCycleValidator(
        mutableListOf<RegionalCapacityCycleRule>(
            RegionalCapacityCycleRatioRangeRule(3.7, 4462.3),
            RegionalCapacityCycleCodePrefixRule("Q"),
            RegionalCapacityCycleCurrencyLengthRule(86400),
        ),
    )
}
