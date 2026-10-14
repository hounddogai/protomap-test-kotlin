package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.text.normalize

/** A rule a adaptive route offset manifest must pass. Check returns its problems, or none. */
interface AdaptiveRouteOffsetManifestRule {
    fun check(item: AdaptiveRouteOffsetManifest): MutableList<String>
}

data class AdaptiveRouteOffsetManifestSourceLengthRule(var maxLength: Int) : AdaptiveRouteOffsetManifestRule {
    override fun check(item: AdaptiveRouteOffsetManifest): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRouteOffsetManifestRequiredLabelRule(var message: String) : AdaptiveRouteOffsetManifestRule {
    override fun check(item: AdaptiveRouteOffsetManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRouteOffsetManifestRequiredSourceRule(
    var message: String,
) : AdaptiveRouteOffsetManifestRule {
    override fun check(item: AdaptiveRouteOffsetManifest): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRouteOffsetManifestCodePrefixRule(var prefix: String) : AdaptiveRouteOffsetManifestRule {
    override fun check(item: AdaptiveRouteOffsetManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRouteOffsetManifestAllowedUnitRule(
    var allowed: MutableList<String>,
) : AdaptiveRouteOffsetManifestRule {
    override fun check(item: AdaptiveRouteOffsetManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is expired")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRouteOffsetManifestQuantityRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveRouteOffsetManifestRule {
    override fun check(item: AdaptiveRouteOffsetManifest): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive route offset manifest. */
data class AdaptiveRouteOffsetManifestValidator(var rules: MutableList<AdaptiveRouteOffsetManifestRule>) {
    fun validate(item: AdaptiveRouteOffsetManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveRouteOffsetManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRouteOffsetManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRouteOffsetManifestValidator(): AdaptiveRouteOffsetManifestValidator {
    return AdaptiveRouteOffsetManifestValidator(
        mutableListOf<AdaptiveRouteOffsetManifestRule>(
            AdaptiveRouteOffsetManifestSourceLengthRule(80),
            AdaptiveRouteOffsetManifestRequiredLabelRule("adaptive route offset manifest needs a value"),
            AdaptiveRouteOffsetManifestRequiredSourceRule("adaptive route offset manifest needs a value"),
            AdaptiveRouteOffsetManifestCodePrefixRule("Q"),
            AdaptiveRouteOffsetManifestAllowedUnitRule(mutableListOf<String>("weighted", "adaptive", "capped")),
            AdaptiveRouteOffsetManifestQuantityRangeRule(64, 86400),
        ),
    )
}
