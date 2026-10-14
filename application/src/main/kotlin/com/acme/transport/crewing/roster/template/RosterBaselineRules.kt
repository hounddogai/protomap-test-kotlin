package com.acme.transport.crewing.roster.template

/** A rule a roster baseline must pass. Check returns its problems, or none. */
interface RosterBaselineRule {
    fun check(item: RosterBaseline): MutableList<String>
}

data class RosterBaselineCodePrefixRule(var prefix: String) : RosterBaselineRule {
    override fun check(item: RosterBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RosterBaselineTrailerIdLengthRule(var maxLength: Int) : RosterBaselineRule {
    override fun check(item: RosterBaseline): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterBaselineScoreRangeRule(var min: Double, var max: Double) : RosterBaselineRule {
    override fun check(item: RosterBaseline): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterBaselineLabelLengthRule(var maxLength: Int) : RosterBaselineRule {
    override fun check(item: RosterBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterBaselineRateRangeRule(var min: Double, var max: Double) : RosterBaselineRule {
    override fun check(item: RosterBaseline): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class RosterBaselineOdometerRangeRule(var min: Int, var max: Int) : RosterBaselineRule {
    override fun check(item: RosterBaseline): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster baseline. */
data class RosterBaselineValidator(var rules: MutableList<RosterBaselineRule>) {
    fun validate(item: RosterBaseline): MutableList<String> {
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

    fun requireValid(item: RosterBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterBaselineValidator(): RosterBaselineValidator {
    return RosterBaselineValidator(
        mutableListOf<RosterBaselineRule>(
            RosterBaselineCodePrefixRule("OPS"),
            RosterBaselineTrailerIdLengthRule(16),
            RosterBaselineScoreRangeRule(9.4, 986.5),
            RosterBaselineLabelLengthRule(86400),
            RosterBaselineRateRangeRule(6.1, 3107.9),
            RosterBaselineOdometerRangeRule(1000, 1000),
        ),
    )
}
