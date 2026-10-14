package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.kit.text.normalize

/** A rule a inspection tier must pass. Check returns its problems, or none. */
interface InspectionTierRule {
    fun check(item: InspectionTier): MutableList<String>
}

data class InspectionTierCodeLengthRule(var maxLength: Int) : InspectionTierRule {
    override fun check(item: InspectionTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionTierReviewerLengthRule(var maxLength: Int) : InspectionTierRule {
    override fun check(item: InspectionTier): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionTierLabelLengthRule(var maxLength: Int) : InspectionTierRule {
    override fun check(item: InspectionTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionTierAllowedRouteCodeRule(var allowed: MutableList<String>) : InspectionTierRule {
    override fun check(item: InspectionTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is missing")
        }
        return mutableListOf<String>()
    }
}

data class InspectionTierRequiredRouteCodeRule(var message: String) : InspectionTierRule {
    override fun check(item: InspectionTier): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection tier. */
data class InspectionTierValidator(var rules: MutableList<InspectionTierRule>) {
    fun validate(item: InspectionTier): MutableList<String> {
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

    fun requireValid(item: InspectionTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionTier>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionTierValidator(): InspectionTierValidator {
    return InspectionTierValidator(
        mutableListOf<InspectionTierRule>(
            InspectionTierCodeLengthRule(3600),
            InspectionTierReviewerLengthRule(64),
            InspectionTierLabelLengthRule(32),
            InspectionTierAllowedRouteCodeRule(mutableListOf<String>("strict")),
            InspectionTierRequiredRouteCodeRule("inspection tier needs an entry"),
        ),
    )
}
