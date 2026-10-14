package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.text.normalize

/** A rule a adaptive service plan manifest must pass. Check returns its problems, or none. */
interface AdaptiveServicePlanManifestRule {
    fun check(item: AdaptiveServicePlanManifest): MutableList<String>
}

data class AdaptiveServicePlanManifestMarginRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveServicePlanManifestRule {
    override fun check(item: AdaptiveServicePlanManifest): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class AdaptiveServicePlanManifestAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveServicePlanManifestRule {
    override fun check(item: AdaptiveServicePlanManifest): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class AdaptiveServicePlanManifestPalletsRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveServicePlanManifestRule {
    override fun check(item: AdaptiveServicePlanManifest): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class AdaptiveServicePlanManifestCodeLengthRule(var maxLength: Int) : AdaptiveServicePlanManifestRule {
    override fun check(item: AdaptiveServicePlanManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveServicePlanManifestAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : AdaptiveServicePlanManifestRule {
    override fun check(item: AdaptiveServicePlanManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveServicePlanManifestRequiredTitleRule(var message: String) : AdaptiveServicePlanManifestRule {
    override fun check(item: AdaptiveServicePlanManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive service plan manifest. */
data class AdaptiveServicePlanManifestValidator(var rules: MutableList<AdaptiveServicePlanManifestRule>) {
    fun validate(item: AdaptiveServicePlanManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveServicePlanManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveServicePlanManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveServicePlanManifestValidator(): AdaptiveServicePlanManifestValidator {
    return AdaptiveServicePlanManifestValidator(
        mutableListOf<AdaptiveServicePlanManifestRule>(
            AdaptiveServicePlanManifestMarginRangeRule(6.7, 3067.7),
            AdaptiveServicePlanManifestAttemptsRangeRule(1000, 86400),
            AdaptiveServicePlanManifestPalletsRangeRule(64, 3600),
            AdaptiveServicePlanManifestCodeLengthRule(255),
            AdaptiveServicePlanManifestAllowedExternalRefRule(mutableListOf<String>("default")),
            AdaptiveServicePlanManifestRequiredTitleRule("adaptive service plan manifest needs a value"),
        ),
    )
}
