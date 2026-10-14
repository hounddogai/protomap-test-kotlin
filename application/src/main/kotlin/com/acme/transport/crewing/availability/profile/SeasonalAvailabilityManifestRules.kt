package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.text.normalize

/** A rule a seasonal availability manifest must pass. Check returns its problems, or none. */
interface SeasonalAvailabilityManifestRule {
    fun check(item: SeasonalAvailabilityManifest): MutableList<String>
}

data class SeasonalAvailabilityManifestAllowedNameRule(
    var allowed: MutableList<String>,
) : SeasonalAvailabilityManifestRule {
    override fun check(item: SeasonalAvailabilityManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAvailabilityManifestDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalAvailabilityManifestRule {
    override fun check(item: SeasonalAvailabilityManifest): MutableList<String> {
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

data class SeasonalAvailabilityManifestRequiredNameRule(
    var message: String,
) : SeasonalAvailabilityManifestRule {
    override fun check(item: SeasonalAvailabilityManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAvailabilityManifestRevisionRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalAvailabilityManifestRule {
    override fun check(item: SeasonalAvailabilityManifest): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal availability manifest. */
data class SeasonalAvailabilityManifestValidator(var rules: MutableList<SeasonalAvailabilityManifestRule>) {
    fun validate(item: SeasonalAvailabilityManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalAvailabilityManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalAvailabilityManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalAvailabilityManifestValidator(): SeasonalAvailabilityManifestValidator {
    return SeasonalAvailabilityManifestValidator(
        mutableListOf<SeasonalAvailabilityManifestRule>(
            SeasonalAvailabilityManifestAllowedNameRule(mutableListOf<String>("seasonal", "fallback")),
            SeasonalAvailabilityManifestDistanceKmRangeRule(7.6, 3123.2),
            SeasonalAvailabilityManifestRequiredNameRule("seasonal availability manifest needs a value"),
            SeasonalAvailabilityManifestRevisionRangeRule(64, 255),
        ),
    )
}
