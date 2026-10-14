package com.acme.transport.crewing.availability.cycle

import com.acme.transport.kit.text.normalize

/** A rule a availability cycle must pass. Check returns its problems, or none. */
interface AvailabilityCycleRule {
    fun check(item: AvailabilityCycle): MutableList<String>
}

data class AvailabilityCycleFuelLitersRangeRule(var min: Double, var max: Double) : AvailabilityCycleRule {
    override fun check(item: AvailabilityCycle): MutableList<String> {
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

data class AvailabilityCycleCodePrefixRule(var prefix: String) : AvailabilityCycleRule {
    override fun check(item: AvailabilityCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityCycleRequiredCodeRule(var message: String) : AvailabilityCycleRule {
    override fun check(item: AvailabilityCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityCycleAllowedCodeRule(var allowed: MutableList<String>) : AvailabilityCycleRule {
    override fun check(item: AvailabilityCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability cycle. */
data class AvailabilityCycleValidator(var rules: MutableList<AvailabilityCycleRule>) {
    fun validate(item: AvailabilityCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilityCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityCycleValidator(): AvailabilityCycleValidator {
    return AvailabilityCycleValidator(
        mutableListOf<AvailabilityCycleRule>(
            AvailabilityCycleFuelLitersRangeRule(3.1, 1395.5),
            AvailabilityCycleCodePrefixRule("OPS"),
            AvailabilityCycleRequiredCodeRule("availability cycle needs an entry"),
            AvailabilityCycleAllowedCodeRule(mutableListOf<String>("strict", "scheduled")),
        ),
    )
}
