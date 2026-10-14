package com.acme.transport.network.zone.zonequota

/** A rule a seasonal zone adjustment must pass. Check returns its problems, or none. */
interface SeasonalZoneAdjustmentRule {
    fun check(item: SeasonalZoneAdjustment): MutableList<String>
}

data class SeasonalZoneAdjustmentCeilingRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalZoneAdjustmentRule {
    override fun check(item: SeasonalZoneAdjustment): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class SeasonalZoneAdjustmentDriverIdLengthRule(var maxLength: Int) : SeasonalZoneAdjustmentRule {
    override fun check(item: SeasonalZoneAdjustment): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalZoneAdjustmentRequiredCodeRule(var message: String) : SeasonalZoneAdjustmentRule {
    override fun check(item: SeasonalZoneAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal zone adjustment. */
data class SeasonalZoneAdjustmentValidator(var rules: MutableList<SeasonalZoneAdjustmentRule>) {
    fun validate(item: SeasonalZoneAdjustment): MutableList<String> {
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

    fun requireValid(item: SeasonalZoneAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalZoneAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalZoneAdjustmentValidator(): SeasonalZoneAdjustmentValidator {
    return SeasonalZoneAdjustmentValidator(
        mutableListOf<SeasonalZoneAdjustmentRule>(
            SeasonalZoneAdjustmentCeilingRangeRule(8.4, 3826.8),
            SeasonalZoneAdjustmentDriverIdLengthRule(32),
            SeasonalZoneAdjustmentRequiredCodeRule("seasonal zone adjustment needs a value"),
        ),
    )
}
