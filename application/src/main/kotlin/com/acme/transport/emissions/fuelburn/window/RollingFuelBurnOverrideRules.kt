package com.acme.transport.emissions.fuelburn.window

/** A rule a rolling fuel burn override must pass. Check returns its problems, or none. */
interface RollingFuelBurnOverrideRule {
    fun check(item: RollingFuelBurnOverride): MutableList<String>
}

data class RollingFuelBurnOverridePhoneLengthRule(var maxLength: Int) : RollingFuelBurnOverrideRule {
    override fun check(item: RollingFuelBurnOverride): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnOverrideRequiredNameRule(var message: String) : RollingFuelBurnOverrideRule {
    override fun check(item: RollingFuelBurnOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnOverrideOdometerRangeRule(var min: Int, var max: Int) : RollingFuelBurnOverrideRule {
    override fun check(item: RollingFuelBurnOverride): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling fuel burn override. */
data class RollingFuelBurnOverrideValidator(var rules: MutableList<RollingFuelBurnOverrideRule>) {
    fun validate(item: RollingFuelBurnOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingFuelBurnOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingFuelBurnOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingFuelBurnOverrideValidator(): RollingFuelBurnOverrideValidator {
    return RollingFuelBurnOverrideValidator(
        mutableListOf<RollingFuelBurnOverrideRule>(
            RollingFuelBurnOverridePhoneLengthRule(1000),
            RollingFuelBurnOverrideRequiredNameRule("rolling fuel burn override needs an entry"),
            RollingFuelBurnOverrideOdometerRangeRule(255, 1000),
        ),
    )
}
