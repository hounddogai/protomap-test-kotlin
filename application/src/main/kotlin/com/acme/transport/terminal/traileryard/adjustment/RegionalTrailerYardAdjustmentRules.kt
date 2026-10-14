package com.acme.transport.terminal.traileryard.adjustment

/** A rule a regional trailer yard adjustment must pass. Check returns its problems, or none. */
interface RegionalTrailerYardAdjustmentRule {
    fun check(item: RegionalTrailerYardAdjustment): MutableList<String>
}

data class RegionalTrailerYardAdjustmentDescriptionLengthRule(
    var maxLength: Int,
) : RegionalTrailerYardAdjustmentRule {
    override fun check(item: RegionalTrailerYardAdjustment): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerYardAdjustmentPalletsRangeRule(
    var min: Int,
    var max: Int,
) : RegionalTrailerYardAdjustmentRule {
    override fun check(item: RegionalTrailerYardAdjustment): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class RegionalTrailerYardAdjustmentApprovedGuardRule(
    var limit: Double,
) : RegionalTrailerYardAdjustmentRule {
    override fun check(item: RegionalTrailerYardAdjustment): MutableList<String> {
        if (item.approved && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional trailer yard adjustment. */
data class RegionalTrailerYardAdjustmentValidator(var rules: MutableList<RegionalTrailerYardAdjustmentRule>) {
    fun validate(item: RegionalTrailerYardAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalTrailerYardAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTrailerYardAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTrailerYardAdjustmentValidator(): RegionalTrailerYardAdjustmentValidator {
    return RegionalTrailerYardAdjustmentValidator(
        mutableListOf<RegionalTrailerYardAdjustmentRule>(
            RegionalTrailerYardAdjustmentDescriptionLengthRule(86400),
            RegionalTrailerYardAdjustmentPalletsRangeRule(64, 86400),
            RegionalTrailerYardAdjustmentApprovedGuardRule(7.8),
        ),
    )
}
