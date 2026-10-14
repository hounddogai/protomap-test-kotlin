package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.kit.text.normalize

/** A rule a regional qualification policy must pass. Check returns its problems, or none. */
interface RegionalQualificationPolicyRule {
    fun check(item: RegionalQualificationPolicy): MutableList<String>
}

data class RegionalQualificationPolicyAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : RegionalQualificationPolicyRule {
    override fun check(item: RegionalQualificationPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationPolicyDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : RegionalQualificationPolicyRule {
    override fun check(item: RegionalQualificationPolicy): MutableList<String> {
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

data class RegionalQualificationPolicyCodePrefixRule(var prefix: String) : RegionalQualificationPolicyRule {
    override fun check(item: RegionalQualificationPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationPolicyDescriptionLengthRule(
    var maxLength: Int,
) : RegionalQualificationPolicyRule {
    override fun check(item: RegionalQualificationPolicy): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationPolicyRequiredRouteCodeRule(
    var message: String,
) : RegionalQualificationPolicyRule {
    override fun check(item: RegionalQualificationPolicy): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationPolicyRequiredNameRule(var message: String) : RegionalQualificationPolicyRule {
    override fun check(item: RegionalQualificationPolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional qualification policy. */
data class RegionalQualificationPolicyValidator(var rules: MutableList<RegionalQualificationPolicyRule>) {
    fun validate(item: RegionalQualificationPolicy): MutableList<String> {
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

    fun requireValid(item: RegionalQualificationPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalQualificationPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalQualificationPolicyValidator(): RegionalQualificationPolicyValidator {
    return RegionalQualificationPolicyValidator(
        mutableListOf<RegionalQualificationPolicyRule>(
            RegionalQualificationPolicyAllowedRouteCodeRule(mutableListOf<String>("primary")),
            RegionalQualificationPolicyDistanceKmRangeRule(5.9, 2397.5),
            RegionalQualificationPolicyCodePrefixRule("REF"),
            RegionalQualificationPolicyDescriptionLengthRule(120),
            RegionalQualificationPolicyRequiredRouteCodeRule("regional qualification policy needs an entry"),
            RegionalQualificationPolicyRequiredNameRule("regional qualification policy needs this field"),
        ),
    )
}
