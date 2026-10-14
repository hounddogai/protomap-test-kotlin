package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.kit.text.normalize

/** A rule a seasonal service area threshold must pass. Check returns its problems, or none. */
interface SeasonalServiceAreaThresholdRule {
    fun check(item: SeasonalServiceAreaThreshold): MutableList<String>
}

data class SeasonalServiceAreaThresholdDriverIdLengthRule(
    var maxLength: Int,
) : SeasonalServiceAreaThresholdRule {
    override fun check(item: SeasonalServiceAreaThreshold): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServiceAreaThresholdCodeLengthRule(var maxLength: Int) : SeasonalServiceAreaThresholdRule {
    override fun check(item: SeasonalServiceAreaThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServiceAreaThresholdCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalServiceAreaThresholdRule {
    override fun check(item: SeasonalServiceAreaThreshold): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class SeasonalServiceAreaThresholdAllowedNotesRule(
    var allowed: MutableList<String>,
) : SeasonalServiceAreaThresholdRule {
    override fun check(item: SeasonalServiceAreaThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServiceAreaThresholdNameLengthRule(var maxLength: Int) : SeasonalServiceAreaThresholdRule {
    override fun check(item: SeasonalServiceAreaThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal service area threshold. */
data class SeasonalServiceAreaThresholdValidator(var rules: MutableList<SeasonalServiceAreaThresholdRule>) {
    fun validate(item: SeasonalServiceAreaThreshold): MutableList<String> {
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

    fun requireValid(item: SeasonalServiceAreaThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalServiceAreaThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalServiceAreaThresholdValidator(): SeasonalServiceAreaThresholdValidator {
    return SeasonalServiceAreaThresholdValidator(
        mutableListOf<SeasonalServiceAreaThresholdRule>(
            SeasonalServiceAreaThresholdDriverIdLengthRule(80),
            SeasonalServiceAreaThresholdCodeLengthRule(255),
            SeasonalServiceAreaThresholdCreatedAtRangeRule(80, 86400),
            SeasonalServiceAreaThresholdAllowedNotesRule(mutableListOf<String>("regional")),
            SeasonalServiceAreaThresholdNameLengthRule(3600),
        ),
    )
}
