package com.acme.transport.fleetops.tire.quota

/** A rule a seasonal tire quota must pass. Check returns its problems, or none. */
interface SeasonalTireQuotaRule {
    fun check(item: SeasonalTireQuota): MutableList<String>
}

data class SeasonalTireQuotaOdometerRangeRule(var min: Int, var max: Int) : SeasonalTireQuotaRule {
    override fun check(item: SeasonalTireQuota): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTireQuotaNameLengthRule(var maxLength: Int) : SeasonalTireQuotaRule {
    override fun check(item: SeasonalTireQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTireQuotaFloorValueRangeRule(var min: Double, var max: Double) : SeasonalTireQuotaRule {
    override fun check(item: SeasonalTireQuota): MutableList<String> {
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

/** Runs every rule of a seasonal tire quota. */
data class SeasonalTireQuotaValidator(var rules: MutableList<SeasonalTireQuotaRule>) {
    fun validate(item: SeasonalTireQuota): MutableList<String> {
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

    fun requireValid(item: SeasonalTireQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTireQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTireQuotaValidator(): SeasonalTireQuotaValidator {
    return SeasonalTireQuotaValidator(
        mutableListOf<SeasonalTireQuotaRule>(
            SeasonalTireQuotaOdometerRangeRule(255, 86400),
            SeasonalTireQuotaNameLengthRule(32),
            SeasonalTireQuotaFloorValueRangeRule(0.5, 4656.8),
        ),
    )
}
