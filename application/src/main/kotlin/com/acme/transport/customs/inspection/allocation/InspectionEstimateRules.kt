package com.acme.transport.customs.inspection.allocation

import com.acme.transport.kit.text.normalize

/** A rule a inspection estimate must pass. Check returns its problems, or none. */
interface InspectionEstimateRule {
    fun check(item: InspectionEstimate): MutableList<String>
}

data class InspectionEstimateStopCountRangeRule(var min: Int, var max: Int) : InspectionEstimateRule {
    override fun check(item: InspectionEstimate): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionEstimateCodePrefixRule(var prefix: String) : InspectionEstimateRule {
    override fun check(item: InspectionEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionEstimateTitleLengthRule(var maxLength: Int) : InspectionEstimateRule {
    override fun check(item: InspectionEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionEstimateRequiredCurrencyRule(var message: String) : InspectionEstimateRule {
    override fun check(item: InspectionEstimate): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionEstimateAllowedCodeRule(var allowed: MutableList<String>) : InspectionEstimateRule {
    override fun check(item: InspectionEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
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
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
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
            InspectionEstimateStopCountRangeRule(16, 80),
            InspectionEstimateCodePrefixRule("OPS"),
            InspectionEstimateTitleLengthRule(1000),
            InspectionEstimateRequiredCurrencyRule("inspection estimate needs this field"),
            InspectionEstimateAllowedCodeRule(mutableListOf<String>("legacy", "tiered", "capped")),
        ),
    )
}
