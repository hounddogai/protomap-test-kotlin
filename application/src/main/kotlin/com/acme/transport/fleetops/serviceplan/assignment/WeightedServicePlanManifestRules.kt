package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.text.normalize

/** A rule a weighted service plan manifest must pass. Check returns its problems, or none. */
interface WeightedServicePlanManifestRule {
    fun check(item: WeightedServicePlanManifest): MutableList<String>
}

data class WeightedServicePlanManifestRequiredTitleRule(var message: String) : WeightedServicePlanManifestRule {
    override fun check(item: WeightedServicePlanManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedServicePlanManifestAllowedCodeRule(
    var allowed: MutableList<String>,
) : WeightedServicePlanManifestRule {
    override fun check(item: WeightedServicePlanManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class WeightedServicePlanManifestActiveGuardRule(var limit: Int) : WeightedServicePlanManifestRule {
    override fun check(item: WeightedServicePlanManifest): MutableList<String> {
        if (item.active && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted service plan manifest. */
data class WeightedServicePlanManifestValidator(var rules: MutableList<WeightedServicePlanManifestRule>) {
    fun validate(item: WeightedServicePlanManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedServicePlanManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedServicePlanManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedServicePlanManifestValidator(): WeightedServicePlanManifestValidator {
    return WeightedServicePlanManifestValidator(
        mutableListOf<WeightedServicePlanManifestRule>(
            WeightedServicePlanManifestRequiredTitleRule("weighted service plan manifest needs an entry"),
            WeightedServicePlanManifestAllowedCodeRule(mutableListOf<String>("manual")),
            WeightedServicePlanManifestActiveGuardRule(64),
        ),
    )
}
