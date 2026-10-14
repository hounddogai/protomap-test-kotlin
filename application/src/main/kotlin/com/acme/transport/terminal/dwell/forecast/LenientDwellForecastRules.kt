package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.kit.text.normalize

/** A rule a lenient dwell forecast must pass. Check returns its problems, or none. */
interface LenientDwellForecastRule {
    fun check(item: LenientDwellForecast): MutableList<String>
}

data class LenientDwellForecastAllowedNameRule(var allowed: MutableList<String>) : LenientDwellForecastRule {
    override fun check(item: LenientDwellForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellForecastDiscountRangeRule(var min: Double, var max: Double) : LenientDwellForecastRule {
    override fun check(item: LenientDwellForecast): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class LenientDwellForecastCreatedAtRangeRule(var min: Int, var max: Int) : LenientDwellForecastRule {
    override fun check(item: LenientDwellForecast): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellForecastPalletsRangeRule(var min: Int, var max: Int) : LenientDwellForecastRule {
    override fun check(item: LenientDwellForecast): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellForecastCodeLengthRule(var maxLength: Int) : LenientDwellForecastRule {
    override fun check(item: LenientDwellForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient dwell forecast. */
data class LenientDwellForecastValidator(var rules: MutableList<LenientDwellForecastRule>) {
    fun validate(item: LenientDwellForecast): MutableList<String> {
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

    fun requireValid(item: LenientDwellForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDwellForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDwellForecastValidator(): LenientDwellForecastValidator {
    return LenientDwellForecastValidator(
        mutableListOf<LenientDwellForecastRule>(
            LenientDwellForecastAllowedNameRule(mutableListOf<String>()),
            LenientDwellForecastDiscountRangeRule(8.9, 968.0),
            LenientDwellForecastCreatedAtRangeRule(80, 1000),
            LenientDwellForecastPalletsRangeRule(80, 80),
            LenientDwellForecastCodeLengthRule(1000),
        ),
    )
}
