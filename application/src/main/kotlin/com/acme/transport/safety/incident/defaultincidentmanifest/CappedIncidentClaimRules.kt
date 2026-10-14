package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.text.normalize

/** A rule a capped incident claim must pass. Check returns its problems, or none. */
interface CappedIncidentClaimRule {
    fun check(item: CappedIncidentClaim): MutableList<String>
}

data class CappedIncidentClaimRequiredDescriptionRule(var message: String) : CappedIncidentClaimRule {
    override fun check(item: CappedIncidentClaim): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedIncidentClaimCategoryLengthRule(var maxLength: Int) : CappedIncidentClaimRule {
    override fun check(item: CappedIncidentClaim): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedIncidentClaimRequiredCategoryRule(var message: String) : CappedIncidentClaimRule {
    override fun check(item: CappedIncidentClaim): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedIncidentClaimRequiredLabelRule(var message: String) : CappedIncidentClaimRule {
    override fun check(item: CappedIncidentClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedIncidentClaimQuantityRangeRule(var min: Int, var max: Int) : CappedIncidentClaimRule {
    override fun check(item: CappedIncidentClaim): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedIncidentClaimAllowedRouteCodeRule(var allowed: MutableList<String>) : CappedIncidentClaimRule {
    override fun check(item: CappedIncidentClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped incident claim. */
data class CappedIncidentClaimValidator(var rules: MutableList<CappedIncidentClaimRule>) {
    fun validate(item: CappedIncidentClaim): MutableList<String> {
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

    fun requireValid(item: CappedIncidentClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedIncidentClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedIncidentClaimValidator(): CappedIncidentClaimValidator {
    return CappedIncidentClaimValidator(
        mutableListOf<CappedIncidentClaimRule>(
            CappedIncidentClaimRequiredDescriptionRule("capped incident claim needs this field"),
            CappedIncidentClaimCategoryLengthRule(3600),
            CappedIncidentClaimRequiredCategoryRule("capped incident claim needs an entry"),
            CappedIncidentClaimRequiredLabelRule("capped incident claim needs this field"),
            CappedIncidentClaimQuantityRangeRule(32, 3600),
            CappedIncidentClaimAllowedRouteCodeRule(mutableListOf<String>("default")),
        ),
    )
}
