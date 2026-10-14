package com.acme.transport.crewing.restbreak.breakvariance

/** A rule a seasonal rest break variance must pass. Check returns its problems, or none. */
interface SeasonalRestBreakVarianceRule {
    fun check(item: SeasonalRestBreakVariance): MutableList<String>
}

data class SeasonalRestBreakVarianceCodeLengthRule(var maxLength: Int) : SeasonalRestBreakVarianceRule {
    override fun check(item: SeasonalRestBreakVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRestBreakVarianceRevisionRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalRestBreakVarianceRule {
    override fun check(item: SeasonalRestBreakVariance): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRestBreakVarianceFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalRestBreakVarianceRule {
    override fun check(item: SeasonalRestBreakVariance): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRestBreakVarianceRequiredCodeRule(var message: String) : SeasonalRestBreakVarianceRule {
    override fun check(item: SeasonalRestBreakVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRestBreakVarianceDriverIdLengthRule(var maxLength: Int) : SeasonalRestBreakVarianceRule {
    override fun check(item: SeasonalRestBreakVariance): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal rest break variance. */
data class SeasonalRestBreakVarianceValidator(var rules: MutableList<SeasonalRestBreakVarianceRule>) {
    fun validate(item: SeasonalRestBreakVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalRestBreakVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRestBreakVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRestBreakVarianceValidator(): SeasonalRestBreakVarianceValidator {
    return SeasonalRestBreakVarianceValidator(
        mutableListOf<SeasonalRestBreakVarianceRule>(
            SeasonalRestBreakVarianceCodeLengthRule(80),
            SeasonalRestBreakVarianceRevisionRangeRule(120, 3600),
            SeasonalRestBreakVarianceFloorValueRangeRule(3.7, 1459.9),
            SeasonalRestBreakVarianceRequiredCodeRule("seasonal rest break variance needs a value"),
            SeasonalRestBreakVarianceDriverIdLengthRule(255),
        ),
    )
}
