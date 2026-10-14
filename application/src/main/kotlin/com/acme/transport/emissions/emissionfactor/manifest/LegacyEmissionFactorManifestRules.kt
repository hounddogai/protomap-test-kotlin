package com.acme.transport.emissions.emissionfactor.manifest

/** A rule a legacy emission factor manifest must pass. Check returns its problems, or none. */
interface LegacyEmissionFactorManifestRule {
    fun check(item: LegacyEmissionFactorManifest): MutableList<String>
}

data class LegacyEmissionFactorManifestWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : LegacyEmissionFactorManifestRule {
    override fun check(item: LegacyEmissionFactorManifest): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class LegacyEmissionFactorManifestNameLengthRule(var maxLength: Int) : LegacyEmissionFactorManifestRule {
    override fun check(item: LegacyEmissionFactorManifest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyEmissionFactorManifestRequiredNameRule(
    var message: String,
) : LegacyEmissionFactorManifestRule {
    override fun check(item: LegacyEmissionFactorManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy emission factor manifest. */
data class LegacyEmissionFactorManifestValidator(var rules: MutableList<LegacyEmissionFactorManifestRule>) {
    fun validate(item: LegacyEmissionFactorManifest): MutableList<String> {
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

    fun requireValid(item: LegacyEmissionFactorManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyEmissionFactorManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyEmissionFactorManifestValidator(): LegacyEmissionFactorManifestValidator {
    return LegacyEmissionFactorManifestValidator(
        mutableListOf<LegacyEmissionFactorManifestRule>(
            LegacyEmissionFactorManifestWindowDaysRangeRule(3600, 86400),
            LegacyEmissionFactorManifestNameLengthRule(16),
            LegacyEmissionFactorManifestRequiredNameRule("legacy emission factor manifest needs this field"),
        ),
    )
}
