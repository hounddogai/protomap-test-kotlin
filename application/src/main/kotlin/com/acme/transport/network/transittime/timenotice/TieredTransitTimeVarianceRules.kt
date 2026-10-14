package com.acme.transport.network.transittime.timenotice

/** A rule a tiered transit time variance must pass. Check returns its problems, or none. */
interface TieredTransitTimeVarianceRule {
    fun check(item: TieredTransitTimeVariance): MutableList<String>
}

data class TieredTransitTimeVarianceRequiredCodeRule(var message: String) : TieredTransitTimeVarianceRule {
    override fun check(item: TieredTransitTimeVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTransitTimeVarianceRequiredLaneRule(var message: String) : TieredTransitTimeVarianceRule {
    override fun check(item: TieredTransitTimeVariance): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTransitTimeVarianceCodeLengthRule(var maxLength: Int) : TieredTransitTimeVarianceRule {
    override fun check(item: TieredTransitTimeVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTransitTimeVarianceDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : TieredTransitTimeVarianceRule {
    override fun check(item: TieredTransitTimeVariance): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered transit time variance. */
data class TieredTransitTimeVarianceValidator(var rules: MutableList<TieredTransitTimeVarianceRule>) {
    fun validate(item: TieredTransitTimeVariance): MutableList<String> {
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

    fun requireValid(item: TieredTransitTimeVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTransitTimeVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTransitTimeVarianceValidator(): TieredTransitTimeVarianceValidator {
    return TieredTransitTimeVarianceValidator(
        mutableListOf<TieredTransitTimeVarianceRule>(
            TieredTransitTimeVarianceRequiredCodeRule("tiered transit time variance needs this field"),
            TieredTransitTimeVarianceRequiredLaneRule("tiered transit time variance needs an entry"),
            TieredTransitTimeVarianceCodeLengthRule(80),
            TieredTransitTimeVarianceDurationMinutesRangeRule(255, 1000),
        ),
    )
}
