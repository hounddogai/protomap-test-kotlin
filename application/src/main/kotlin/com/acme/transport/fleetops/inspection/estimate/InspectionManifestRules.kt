package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.kit.text.normalize

/** A rule a inspection manifest must pass. Check returns its problems, or none. */
interface InspectionManifestRule {
    fun check(item: InspectionManifest): MutableList<String>
}

data class InspectionManifestTitleLengthRule(var maxLength: Int) : InspectionManifestRule {
    override fun check(item: InspectionManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionManifestAllowedCarrierRule(var allowed: MutableList<String>) : InspectionManifestRule {
    override fun check(item: InspectionManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is too long")
        }
        return mutableListOf<String>()
    }
}

data class InspectionManifestAmountRangeRule(var min: Double, var max: Double) : InspectionManifestRule {
    override fun check(item: InspectionManifest): MutableList<String> {
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

data class InspectionManifestBudgetRangeRule(var min: Double, var max: Double) : InspectionManifestRule {
    override fun check(item: InspectionManifest): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionManifestCodeLengthRule(var maxLength: Int) : InspectionManifestRule {
    override fun check(item: InspectionManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection manifest. */
data class InspectionManifestValidator(var rules: MutableList<InspectionManifestRule>) {
    fun validate(item: InspectionManifest): MutableList<String> {
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

    fun requireValid(item: InspectionManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionManifestValidator(): InspectionManifestValidator {
    return InspectionManifestValidator(
        mutableListOf<InspectionManifestRule>(
            InspectionManifestTitleLengthRule(86400),
            InspectionManifestAllowedCarrierRule(mutableListOf<String>("primary")),
            InspectionManifestAmountRangeRule(1.2, 792.1),
            InspectionManifestBudgetRangeRule(7.3, 3680.7),
            InspectionManifestCodeLengthRule(16),
        ),
    )
}
