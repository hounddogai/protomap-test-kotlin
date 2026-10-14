package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.text.normalize

/** A rule a adaptive inspection draft must pass. Check returns its problems, or none. */
interface AdaptiveInspectionDraftRule {
    fun check(item: AdaptiveInspectionDraft): MutableList<String>
}

data class AdaptiveInspectionDraftFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveInspectionDraftRule {
    override fun check(item: AdaptiveInspectionDraft): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class AdaptiveInspectionDraftAllowedDepotRule(
    var allowed: MutableList<String>,
) : AdaptiveInspectionDraftRule {
    override fun check(item: AdaptiveInspectionDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is missing")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionDraftCountryLengthRule(var maxLength: Int) : AdaptiveInspectionDraftRule {
    override fun check(item: AdaptiveInspectionDraft): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionDraftDepotLengthRule(var maxLength: Int) : AdaptiveInspectionDraftRule {
    override fun check(item: AdaptiveInspectionDraft): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionDraftLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveInspectionDraftRule {
    override fun check(item: AdaptiveInspectionDraft): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a adaptive inspection draft. */
data class AdaptiveInspectionDraftValidator(var rules: MutableList<AdaptiveInspectionDraftRule>) {
    fun validate(item: AdaptiveInspectionDraft): MutableList<String> {
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

    fun requireValid(item: AdaptiveInspectionDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionDraftValidator(): AdaptiveInspectionDraftValidator {
    return AdaptiveInspectionDraftValidator(
        mutableListOf<AdaptiveInspectionDraftRule>(
            AdaptiveInspectionDraftFuelLitersRangeRule(5.8, 3608.9),
            AdaptiveInspectionDraftAllowedDepotRule(mutableListOf<String>("capped", "tiered")),
            AdaptiveInspectionDraftCountryLengthRule(1000),
            AdaptiveInspectionDraftDepotLengthRule(64),
            AdaptiveInspectionDraftLoadKgRangeRule(7.2, 2105.9),
        ),
    )
}
