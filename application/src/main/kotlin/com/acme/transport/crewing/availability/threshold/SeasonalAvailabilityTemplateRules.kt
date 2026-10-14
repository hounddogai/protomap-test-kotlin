package com.acme.transport.crewing.availability.threshold

import com.acme.transport.kit.text.normalize

/** A rule a seasonal availability template must pass. Check returns its problems, or none. */
interface SeasonalAvailabilityTemplateRule {
    fun check(item: SeasonalAvailabilityTemplate): MutableList<String>
}

data class SeasonalAvailabilityTemplateEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalAvailabilityTemplateRule {
    override fun check(item: SeasonalAvailabilityTemplate): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class SeasonalAvailabilityTemplateRequiredCodeRule(
    var message: String,
) : SeasonalAvailabilityTemplateRule {
    override fun check(item: SeasonalAvailabilityTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAvailabilityTemplateRequiredNameRule(
    var message: String,
) : SeasonalAvailabilityTemplateRule {
    override fun check(item: SeasonalAvailabilityTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAvailabilityTemplateAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalAvailabilityTemplateRule {
    override fun check(item: SeasonalAvailabilityTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAvailabilityTemplateRequiredSegmentRule(
    var message: String,
) : SeasonalAvailabilityTemplateRule {
    override fun check(item: SeasonalAvailabilityTemplate): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal availability template. */
data class SeasonalAvailabilityTemplateValidator(var rules: MutableList<SeasonalAvailabilityTemplateRule>) {
    fun validate(item: SeasonalAvailabilityTemplate): MutableList<String> {
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

    fun requireValid(item: SeasonalAvailabilityTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalAvailabilityTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalAvailabilityTemplateValidator(): SeasonalAvailabilityTemplateValidator {
    return SeasonalAvailabilityTemplateValidator(
        mutableListOf<SeasonalAvailabilityTemplateRule>(
            SeasonalAvailabilityTemplateEtaMinutesRangeRule(16, 86400),
            SeasonalAvailabilityTemplateRequiredCodeRule("seasonal availability template needs an entry"),
            SeasonalAvailabilityTemplateRequiredNameRule("seasonal availability template needs this field"),
            SeasonalAvailabilityTemplateAllowedCodeRule(mutableListOf<String>("strict", "manual")),
            SeasonalAvailabilityTemplateRequiredSegmentRule("seasonal availability template needs a value"),
        ),
    )
}
