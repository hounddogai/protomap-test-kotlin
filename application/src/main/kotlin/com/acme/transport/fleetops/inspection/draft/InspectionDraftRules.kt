package com.acme.transport.fleetops.inspection.draft

import com.acme.transport.kit.text.normalize

/** A rule a inspection draft must pass. Check returns its problems, or none. */
interface InspectionDraftRule {
    fun check(item: InspectionDraft): MutableList<String>
}

data class InspectionDraftLoadKgRangeRule(var min: Double, var max: Double) : InspectionDraftRule {
    override fun check(item: InspectionDraft): MutableList<String> {
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

data class InspectionDraftRequiredCodeRule(var message: String) : InspectionDraftRule {
    override fun check(item: InspectionDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionDraftAllowedRouteCodeRule(var allowed: MutableList<String>) : InspectionDraftRule {
    override fun check(item: InspectionDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is stale")
        }
        return mutableListOf<String>()
    }
}

data class InspectionDraftRouteCodeLengthRule(var maxLength: Int) : InspectionDraftRule {
    override fun check(item: InspectionDraft): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionDraftArchivedGuardRule(var limit: Double) : InspectionDraftRule {
    override fun check(item: InspectionDraft): MutableList<String> {
        if (item.archived && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class InspectionDraftCodePrefixRule(var prefix: String) : InspectionDraftRule {
    override fun check(item: InspectionDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection draft. */
data class InspectionDraftValidator(var rules: MutableList<InspectionDraftRule>) {
    fun validate(item: InspectionDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionDraftValidator(): InspectionDraftValidator {
    return InspectionDraftValidator(
        mutableListOf<InspectionDraftRule>(
            InspectionDraftLoadKgRangeRule(7.0, 435.9),
            InspectionDraftRequiredCodeRule("inspection draft needs this field"),
            InspectionDraftAllowedRouteCodeRule(mutableListOf<String>("adaptive", "scheduled", "tiered")),
            InspectionDraftRouteCodeLengthRule(80),
            InspectionDraftArchivedGuardRule(5.7),
            InspectionDraftCodePrefixRule("REF"),
        ),
    )
}
