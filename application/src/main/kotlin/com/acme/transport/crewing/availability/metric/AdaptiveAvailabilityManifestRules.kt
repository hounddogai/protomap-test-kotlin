package com.acme.transport.crewing.availability.metric

import com.acme.transport.kit.text.normalize

/** A rule a adaptive availability manifest must pass. Check returns its problems, or none. */
interface AdaptiveAvailabilityManifestRule {
    fun check(item: AdaptiveAvailabilityManifest): MutableList<String>
}

data class AdaptiveAvailabilityManifestAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveAvailabilityManifestRule {
    override fun check(item: AdaptiveAvailabilityManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityManifestRequiredLabelRule(
    var message: String,
) : AdaptiveAvailabilityManifestRule {
    override fun check(item: AdaptiveAvailabilityManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityManifestCodePrefixRule(var prefix: String) : AdaptiveAvailabilityManifestRule {
    override fun check(item: AdaptiveAvailabilityManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityManifestRequiredCodeRule(
    var message: String,
) : AdaptiveAvailabilityManifestRule {
    override fun check(item: AdaptiveAvailabilityManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityManifestThresholdRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveAvailabilityManifestRule {
    override fun check(item: AdaptiveAvailabilityManifest): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class AdaptiveAvailabilityManifestLabelLengthRule(var maxLength: Int) : AdaptiveAvailabilityManifestRule {
    override fun check(item: AdaptiveAvailabilityManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive availability manifest. */
data class AdaptiveAvailabilityManifestValidator(var rules: MutableList<AdaptiveAvailabilityManifestRule>) {
    fun validate(item: AdaptiveAvailabilityManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveAvailabilityManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAvailabilityManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAvailabilityManifestValidator(): AdaptiveAvailabilityManifestValidator {
    return AdaptiveAvailabilityManifestValidator(
        mutableListOf<AdaptiveAvailabilityManifestRule>(
            AdaptiveAvailabilityManifestAllowedCodeRule(mutableListOf<String>("regional", "global", "tiered")),
            AdaptiveAvailabilityManifestRequiredLabelRule("adaptive availability manifest needs a value"),
            AdaptiveAvailabilityManifestCodePrefixRule("REF"),
            AdaptiveAvailabilityManifestRequiredCodeRule("adaptive availability manifest needs an entry"),
            AdaptiveAvailabilityManifestThresholdRangeRule(8.3, 1425.7),
            AdaptiveAvailabilityManifestLabelLengthRule(3600),
        ),
    )
}
