package com.acme.transport.emissions.fuelburn.burnvariance

/** A rule a regional fuel burn record must pass. Check returns its problems, or none. */
interface RegionalFuelBurnRecordRule {
    fun check(item: RegionalFuelBurnRecord): MutableList<String>
}

data class RegionalFuelBurnRecordRequiredCountryRule(var message: String) : RegionalFuelBurnRecordRule {
    override fun check(item: RegionalFuelBurnRecord): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnRecordRequiredCodeRule(var message: String) : RegionalFuelBurnRecordRule {
    override fun check(item: RegionalFuelBurnRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnRecordSummaryLengthRule(var maxLength: Int) : RegionalFuelBurnRecordRule {
    override fun check(item: RegionalFuelBurnRecord): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnRecordDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : RegionalFuelBurnRecordRule {
    override fun check(item: RegionalFuelBurnRecord): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnRecordRequiredTitleRule(var message: String) : RegionalFuelBurnRecordRule {
    override fun check(item: RegionalFuelBurnRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnRecordVersionRangeRule(var min: Int, var max: Int) : RegionalFuelBurnRecordRule {
    override fun check(item: RegionalFuelBurnRecord): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional fuel burn record. */
data class RegionalFuelBurnRecordValidator(var rules: MutableList<RegionalFuelBurnRecordRule>) {
    fun validate(item: RegionalFuelBurnRecord): MutableList<String> {
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

    fun requireValid(item: RegionalFuelBurnRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalFuelBurnRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalFuelBurnRecordValidator(): RegionalFuelBurnRecordValidator {
    return RegionalFuelBurnRecordValidator(
        mutableListOf<RegionalFuelBurnRecordRule>(
            RegionalFuelBurnRecordRequiredCountryRule("regional fuel burn record needs this field"),
            RegionalFuelBurnRecordRequiredCodeRule("regional fuel burn record needs this field"),
            RegionalFuelBurnRecordSummaryLengthRule(1000),
            RegionalFuelBurnRecordDistanceKmRangeRule(5.8, 4498.2),
            RegionalFuelBurnRecordRequiredTitleRule("regional fuel burn record needs this field"),
            RegionalFuelBurnRecordVersionRangeRule(64, 120),
        ),
    )
}
