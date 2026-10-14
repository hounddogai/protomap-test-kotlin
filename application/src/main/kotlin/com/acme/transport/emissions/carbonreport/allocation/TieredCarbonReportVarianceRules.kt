package com.acme.transport.emissions.carbonreport.allocation

/** A rule a tiered carbon report variance must pass. Check returns its problems, or none. */
interface TieredCarbonReportVarianceRule {
    fun check(item: TieredCarbonReportVariance): MutableList<String>
}

data class TieredCarbonReportVarianceNameLengthRule(var maxLength: Int) : TieredCarbonReportVarianceRule {
    override fun check(item: TieredCarbonReportVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCarbonReportVarianceDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : TieredCarbonReportVarianceRule {
    override fun check(item: TieredCarbonReportVariance): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class TieredCarbonReportVarianceVehicleIdLengthRule(var maxLength: Int) : TieredCarbonReportVarianceRule {
    override fun check(item: TieredCarbonReportVariance): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered carbon report variance. */
data class TieredCarbonReportVarianceValidator(var rules: MutableList<TieredCarbonReportVarianceRule>) {
    fun validate(item: TieredCarbonReportVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredCarbonReportVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCarbonReportVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCarbonReportVarianceValidator(): TieredCarbonReportVarianceValidator {
    return TieredCarbonReportVarianceValidator(
        mutableListOf<TieredCarbonReportVarianceRule>(
            TieredCarbonReportVarianceNameLengthRule(32),
            TieredCarbonReportVarianceDurationMinutesRangeRule(120, 1000),
            TieredCarbonReportVarianceVehicleIdLengthRule(64),
        ),
    )
}
