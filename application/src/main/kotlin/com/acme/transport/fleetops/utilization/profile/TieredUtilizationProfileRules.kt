package com.acme.transport.fleetops.utilization.profile

/** A rule a tiered utilization profile must pass. Check returns its problems, or none. */
interface TieredUtilizationProfileRule {
    fun check(item: TieredUtilizationProfile): MutableList<String>
}

data class TieredUtilizationProfileNameLengthRule(var maxLength: Int) : TieredUtilizationProfileRule {
    override fun check(item: TieredUtilizationProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredUtilizationProfileExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : TieredUtilizationProfileRule {
    override fun check(item: TieredUtilizationProfile): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class TieredUtilizationProfileRevisionRangeRule(
    var min: Int,
    var max: Int,
) : TieredUtilizationProfileRule {
    override fun check(item: TieredUtilizationProfile): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered utilization profile. */
data class TieredUtilizationProfileValidator(var rules: MutableList<TieredUtilizationProfileRule>) {
    fun validate(item: TieredUtilizationProfile): MutableList<String> {
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

    fun requireValid(item: TieredUtilizationProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredUtilizationProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredUtilizationProfileValidator(): TieredUtilizationProfileValidator {
    return TieredUtilizationProfileValidator(
        mutableListOf<TieredUtilizationProfileRule>(
            TieredUtilizationProfileNameLengthRule(255),
            TieredUtilizationProfileExpiresAtRangeRule(3600, 86400),
            TieredUtilizationProfileRevisionRangeRule(32, 1000),
        ),
    )
}
