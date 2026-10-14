package com.acme.transport.fleetops.utilization.utilizationrecord

/** A rule a seasonal utilization preference must pass. Check returns its problems, or none. */
interface SeasonalUtilizationPreferenceRule {
    fun check(item: SeasonalUtilizationPreference): MutableList<String>
}

data class SeasonalUtilizationPreferenceLimitRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalUtilizationPreferenceRule {
    override fun check(item: SeasonalUtilizationPreference): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalUtilizationPreferenceMinimumRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalUtilizationPreferenceRule {
    override fun check(item: SeasonalUtilizationPreference): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class SeasonalUtilizationPreferenceNameLengthRule(var maxLength: Int) : SeasonalUtilizationPreferenceRule {
    override fun check(item: SeasonalUtilizationPreference): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalUtilizationPreferenceCodeLengthRule(var maxLength: Int) : SeasonalUtilizationPreferenceRule {
    override fun check(item: SeasonalUtilizationPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalUtilizationPreferenceRequiredDriverIdRule(
    var message: String,
) : SeasonalUtilizationPreferenceRule {
    override fun check(item: SeasonalUtilizationPreference): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal utilization preference. */
data class SeasonalUtilizationPreferenceValidator(var rules: MutableList<SeasonalUtilizationPreferenceRule>) {
    fun validate(item: SeasonalUtilizationPreference): MutableList<String> {
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

    fun requireValid(item: SeasonalUtilizationPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalUtilizationPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalUtilizationPreferenceValidator(): SeasonalUtilizationPreferenceValidator {
    return SeasonalUtilizationPreferenceValidator(
        mutableListOf<SeasonalUtilizationPreferenceRule>(
            SeasonalUtilizationPreferenceLimitRangeRule(120, 255),
            SeasonalUtilizationPreferenceMinimumRangeRule(10.0, 2523.7),
            SeasonalUtilizationPreferenceNameLengthRule(1000),
            SeasonalUtilizationPreferenceCodeLengthRule(120),
            SeasonalUtilizationPreferenceRequiredDriverIdRule("seasonal utilization preference needs a value"),
        ),
    )
}
