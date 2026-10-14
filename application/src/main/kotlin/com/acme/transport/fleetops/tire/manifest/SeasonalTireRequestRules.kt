package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.kit.text.normalize

/** A rule a seasonal tire request must pass. Check returns its problems, or none. */
interface SeasonalTireRequestRule {
    fun check(item: SeasonalTireRequest): MutableList<String>
}

data class SeasonalTireRequestDurationMinutesRangeRule(var min: Int, var max: Int) : SeasonalTireRequestRule {
    override fun check(item: SeasonalTireRequest): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTireRequestCodePrefixRule(var prefix: String) : SeasonalTireRequestRule {
    override fun check(item: SeasonalTireRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTireRequestDistanceKmRangeRule(var min: Double, var max: Double) : SeasonalTireRequestRule {
    override fun check(item: SeasonalTireRequest): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class SeasonalTireRequestCodeLengthRule(var maxLength: Int) : SeasonalTireRequestRule {
    override fun check(item: SeasonalTireRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTireRequestAllowedCodeRule(var allowed: MutableList<String>) : SeasonalTireRequestRule {
    override fun check(item: SeasonalTireRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal tire request. */
data class SeasonalTireRequestValidator(var rules: MutableList<SeasonalTireRequestRule>) {
    fun validate(item: SeasonalTireRequest): MutableList<String> {
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

    fun requireValid(item: SeasonalTireRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTireRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTireRequestValidator(): SeasonalTireRequestValidator {
    return SeasonalTireRequestValidator(
        mutableListOf<SeasonalTireRequestRule>(
            SeasonalTireRequestDurationMinutesRangeRule(255, 1000),
            SeasonalTireRequestCodePrefixRule("ACM"),
            SeasonalTireRequestDistanceKmRangeRule(6.7, 2624.5),
            SeasonalTireRequestCodeLengthRule(255),
            SeasonalTireRequestAllowedCodeRule(mutableListOf<String>("capped", "fallback")),
        ),
    )
}
