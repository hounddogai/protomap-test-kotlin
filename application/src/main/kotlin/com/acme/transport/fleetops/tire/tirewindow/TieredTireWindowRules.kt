package com.acme.transport.fleetops.tire.tirewindow

/** A rule a tiered tire window must pass. Check returns its problems, or none. */
interface TieredTireWindowRule {
    fun check(item: TieredTireWindow): MutableList<String>
}

data class TieredTireWindowCodePrefixRule(var prefix: String) : TieredTireWindowRule {
    override fun check(item: TieredTireWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredTireWindowCountryLengthRule(var maxLength: Int) : TieredTireWindowRule {
    override fun check(item: TieredTireWindow): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTireWindowScoreRangeRule(var min: Double, var max: Double) : TieredTireWindowRule {
    override fun check(item: TieredTireWindow): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered tire window. */
data class TieredTireWindowValidator(var rules: MutableList<TieredTireWindowRule>) {
    fun validate(item: TieredTireWindow): MutableList<String> {
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

    fun requireValid(item: TieredTireWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTireWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTireWindowValidator(): TieredTireWindowValidator {
    return TieredTireWindowValidator(
        mutableListOf<TieredTireWindowRule>(
            TieredTireWindowCodePrefixRule("OPS"),
            TieredTireWindowCountryLengthRule(1000),
            TieredTireWindowScoreRangeRule(3.9, 1276.4),
        ),
    )
}
