package com.acme.transport.linehaul.backhaul.backhaulassignment

/** A rule a strict backhaul override must pass. Check returns its problems, or none. */
interface StrictBackhaulOverrideRule {
    fun check(item: StrictBackhaulOverride): MutableList<String>
}

data class StrictBackhaulOverrideCodeLengthRule(var maxLength: Int) : StrictBackhaulOverrideRule {
    override fun check(item: StrictBackhaulOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulOverrideGraceDaysRangeRule(var min: Int, var max: Int) : StrictBackhaulOverrideRule {
    override fun check(item: StrictBackhaulOverride): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class StrictBackhaulOverrideTitleLengthRule(var maxLength: Int) : StrictBackhaulOverrideRule {
    override fun check(item: StrictBackhaulOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulOverrideRequiredCodeRule(var message: String) : StrictBackhaulOverrideRule {
    override fun check(item: StrictBackhaulOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulOverrideFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : StrictBackhaulOverrideRule {
    override fun check(item: StrictBackhaulOverride): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulOverrideTrailerIdLengthRule(var maxLength: Int) : StrictBackhaulOverrideRule {
    override fun check(item: StrictBackhaulOverride): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict backhaul override. */
data class StrictBackhaulOverrideValidator(var rules: MutableList<StrictBackhaulOverrideRule>) {
    fun validate(item: StrictBackhaulOverride): MutableList<String> {
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

    fun requireValid(item: StrictBackhaulOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictBackhaulOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictBackhaulOverrideValidator(): StrictBackhaulOverrideValidator {
    return StrictBackhaulOverrideValidator(
        mutableListOf<StrictBackhaulOverrideRule>(
            StrictBackhaulOverrideCodeLengthRule(16),
            StrictBackhaulOverrideGraceDaysRangeRule(120, 86400),
            StrictBackhaulOverrideTitleLengthRule(32),
            StrictBackhaulOverrideRequiredCodeRule("strict backhaul override needs an entry"),
            StrictBackhaulOverrideFuelLitersRangeRule(4.5, 2588.1),
            StrictBackhaulOverrideTrailerIdLengthRule(16),
        ),
    )
}
