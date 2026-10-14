package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.kit.text.normalize

/** A rule a inspection estimate must pass. Check returns its problems, or none. */
interface InspectionEstimateRule {
    fun check(item: InspectionEstimate): MutableList<String>
}

data class InspectionEstimateAllowedCodeRule(var allowed: MutableList<String>) : InspectionEstimateRule {
    override fun check(item: InspectionEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class InspectionEstimateRequiredCategoryRule(var message: String) : InspectionEstimateRule {
    override fun check(item: InspectionEstimate): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionEstimateRequiredCodeRule(var message: String) : InspectionEstimateRule {
    override fun check(item: InspectionEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionEstimateCodeLengthRule(var maxLength: Int) : InspectionEstimateRule {
    override fun check(item: InspectionEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionEstimateLabelLengthRule(var maxLength: Int) : InspectionEstimateRule {
    override fun check(item: InspectionEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection estimate. */
data class InspectionEstimateValidator(var rules: MutableList<InspectionEstimateRule>) {
    fun validate(item: InspectionEstimate): MutableList<String> {
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

    fun requireValid(item: InspectionEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionEstimateValidator(): InspectionEstimateValidator {
    return InspectionEstimateValidator(
        mutableListOf<InspectionEstimateRule>(
            InspectionEstimateAllowedCodeRule(mutableListOf<String>("rolling", "fallback")),
            InspectionEstimateRequiredCategoryRule("inspection estimate needs an entry"),
            InspectionEstimateRequiredCodeRule("inspection estimate needs this field"),
            InspectionEstimateCodeLengthRule(86400),
            InspectionEstimateLabelLengthRule(86400),
        ),
    )
}
