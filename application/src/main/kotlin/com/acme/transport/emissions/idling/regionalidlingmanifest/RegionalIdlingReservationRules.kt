package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.text.normalize

/** A rule a regional idling reservation must pass. Check returns its problems, or none. */
interface RegionalIdlingReservationRule {
    fun check(item: RegionalIdlingReservation): MutableList<String>
}

data class RegionalIdlingReservationAllowedLocaleRule(
    var allowed: MutableList<String>,
) : RegionalIdlingReservationRule {
    override fun check(item: RegionalIdlingReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingReservationRatioRangeRule(
    var min: Double,
    var max: Double,
) : RegionalIdlingReservationRule {
    override fun check(item: RegionalIdlingReservation): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingReservationTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RegionalIdlingReservationRule {
    override fun check(item: RegionalIdlingReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class RegionalIdlingReservationRequiredLabelRule(var message: String) : RegionalIdlingReservationRule {
    override fun check(item: RegionalIdlingReservation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingReservationLocaleLengthRule(var maxLength: Int) : RegionalIdlingReservationRule {
    override fun check(item: RegionalIdlingReservation): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingReservationCodeLengthRule(var maxLength: Int) : RegionalIdlingReservationRule {
    override fun check(item: RegionalIdlingReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional idling reservation. */
data class RegionalIdlingReservationValidator(var rules: MutableList<RegionalIdlingReservationRule>) {
    fun validate(item: RegionalIdlingReservation): MutableList<String> {
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

    fun requireValid(item: RegionalIdlingReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalIdlingReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalIdlingReservationValidator(): RegionalIdlingReservationValidator {
    return RegionalIdlingReservationValidator(
        mutableListOf<RegionalIdlingReservationRule>(
            RegionalIdlingReservationAllowedLocaleRule(mutableListOf<String>("capped", "fallback", "legacy")),
            RegionalIdlingReservationRatioRangeRule(7.5, 2700.7),
            RegionalIdlingReservationTagsLimitRule(32, mutableListOf<String>("seasonal")),
            RegionalIdlingReservationRequiredLabelRule("regional idling reservation needs an entry"),
            RegionalIdlingReservationLocaleLengthRule(16),
            RegionalIdlingReservationCodeLengthRule(1000),
        ),
    )
}
