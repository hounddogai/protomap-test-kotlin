package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.text.normalize

/** A rule a global service plan manifest must pass. Check returns its problems, or none. */
interface GlobalServicePlanManifestRule {
    fun check(item: GlobalServicePlanManifest): MutableList<String>
}

data class GlobalServicePlanManifestAmountRangeRule(
    var min: Double,
    var max: Double,
) : GlobalServicePlanManifestRule {
    override fun check(item: GlobalServicePlanManifest): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class GlobalServicePlanManifestOwnerIdLengthRule(var maxLength: Int) : GlobalServicePlanManifestRule {
    override fun check(item: GlobalServicePlanManifest): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalServicePlanManifestRequiredLabelRule(var message: String) : GlobalServicePlanManifestRule {
    override fun check(item: GlobalServicePlanManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalServicePlanManifestAllowedLabelRule(
    var allowed: MutableList<String>,
) : GlobalServicePlanManifestRule {
    override fun check(item: GlobalServicePlanManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global service plan manifest. */
data class GlobalServicePlanManifestValidator(var rules: MutableList<GlobalServicePlanManifestRule>) {
    fun validate(item: GlobalServicePlanManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalServicePlanManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalServicePlanManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalServicePlanManifestValidator(): GlobalServicePlanManifestValidator {
    return GlobalServicePlanManifestValidator(
        mutableListOf<GlobalServicePlanManifestRule>(
            GlobalServicePlanManifestAmountRangeRule(0.2, 3081.4),
            GlobalServicePlanManifestOwnerIdLengthRule(255),
            GlobalServicePlanManifestRequiredLabelRule("global service plan manifest needs this field"),
            GlobalServicePlanManifestAllowedLabelRule(mutableListOf<String>("default", "primary", "capped")),
        ),
    )
}
