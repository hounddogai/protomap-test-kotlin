package com.acme.transport.lastmile.parcel.parcelvariance

/** A rule a weighted parcel forecast must pass. Check returns its problems, or none. */
interface WeightedParcelForecastRule {
    fun check(item: WeightedParcelForecast): MutableList<String>
}

data class WeightedParcelForecastRequiredShiftCodeRule(var message: String) : WeightedParcelForecastRule {
    override fun check(item: WeightedParcelForecast): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedParcelForecastRequiredChecksumRule(var message: String) : WeightedParcelForecastRule {
    override fun check(item: WeightedParcelForecast): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedParcelForecastChecksumLengthRule(var maxLength: Int) : WeightedParcelForecastRule {
    override fun check(item: WeightedParcelForecast): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted parcel forecast. */
data class WeightedParcelForecastValidator(var rules: MutableList<WeightedParcelForecastRule>) {
    fun validate(item: WeightedParcelForecast): MutableList<String> {
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

    fun requireValid(item: WeightedParcelForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedParcelForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedParcelForecastValidator(): WeightedParcelForecastValidator {
    return WeightedParcelForecastValidator(
        mutableListOf<WeightedParcelForecastRule>(
            WeightedParcelForecastRequiredShiftCodeRule("weighted parcel forecast needs a value"),
            WeightedParcelForecastRequiredChecksumRule("weighted parcel forecast needs a value"),
            WeightedParcelForecastChecksumLengthRule(1000),
        ),
    )
}
