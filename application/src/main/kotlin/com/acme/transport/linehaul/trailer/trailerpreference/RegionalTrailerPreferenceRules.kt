package com.acme.transport.linehaul.trailer.trailerpreference

import com.acme.transport.kit.text.normalize

/** A rule a regional trailer preference must pass. Check returns its problems, or none. */
interface RegionalTrailerPreferenceRule {
    fun check(item: RegionalTrailerPreference): MutableList<String>
}

data class RegionalTrailerPreferenceAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : RegionalTrailerPreferenceRule {
    override fun check(item: RegionalTrailerPreference): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerPreferenceRequiredTitleRule(var message: String) : RegionalTrailerPreferenceRule {
    override fun check(item: RegionalTrailerPreference): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerPreferenceRegionLengthRule(var maxLength: Int) : RegionalTrailerPreferenceRule {
    override fun check(item: RegionalTrailerPreference): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerPreferenceRequiredVehicleIdRule(var message: String) : RegionalTrailerPreferenceRule {
    override fun check(item: RegionalTrailerPreference): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerPreferenceAllowedTitleRule(
    var allowed: MutableList<String>,
) : RegionalTrailerPreferenceRule {
    override fun check(item: RegionalTrailerPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional trailer preference. */
data class RegionalTrailerPreferenceValidator(var rules: MutableList<RegionalTrailerPreferenceRule>) {
    fun validate(item: RegionalTrailerPreference): MutableList<String> {
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

    fun requireValid(item: RegionalTrailerPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTrailerPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTrailerPreferenceValidator(): RegionalTrailerPreferenceValidator {
    return RegionalTrailerPreferenceValidator(
        mutableListOf<RegionalTrailerPreferenceRule>(
            RegionalTrailerPreferenceAttemptsRangeRule(16, 16),
            RegionalTrailerPreferenceRequiredTitleRule("regional trailer preference needs this field"),
            RegionalTrailerPreferenceRegionLengthRule(64),
            RegionalTrailerPreferenceRequiredVehicleIdRule("regional trailer preference needs an entry"),
            RegionalTrailerPreferenceAllowedTitleRule(mutableListOf<String>("pending", "capped")),
        ),
    )
}
