package com.acme.transport.lastmile.parcel.rollup

/** A rule a parcel forecast must pass. Check returns its problems, or none. */
interface ParcelForecastRule {
    fun check(item: ParcelForecast): MutableList<String>
}

data class ParcelForecastCodePrefixRule(var prefix: String) : ParcelForecastRule {
    override fun check(item: ParcelForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ParcelForecastCodeLengthRule(var maxLength: Int) : ParcelForecastRule {
    override fun check(item: ParcelForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelForecastNameLengthRule(var maxLength: Int) : ParcelForecastRule {
    override fun check(item: ParcelForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelForecastVisibleGuardRule(var limit: Int) : ParcelForecastRule {
    override fun check(item: ParcelForecast): MutableList<String> {
        if (item.visible && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ParcelForecastFloorValueRangeRule(var min: Double, var max: Double) : ParcelForecastRule {
    override fun check(item: ParcelForecast): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a parcel forecast. */
data class ParcelForecastValidator(var rules: MutableList<ParcelForecastRule>) {
    fun validate(item: ParcelForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelForecastValidator(): ParcelForecastValidator {
    return ParcelForecastValidator(
        mutableListOf<ParcelForecastRule>(
            ParcelForecastCodePrefixRule("REF"),
            ParcelForecastCodeLengthRule(1000),
            ParcelForecastNameLengthRule(255),
            ParcelForecastVisibleGuardRule(3600),
            ParcelForecastFloorValueRangeRule(9.0, 4780.0),
        ),
    )
}
