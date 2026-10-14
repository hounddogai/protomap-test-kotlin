package com.acme.transport.linehaul.tender.estimate

import com.acme.transport.kit.text.normalize

/** A rule a tender estimate must pass. Check returns its problems, or none. */
interface TenderEstimateRule {
    fun check(item: TenderEstimate): MutableList<String>
}

data class TenderEstimateCodeLengthRule(var maxLength: Int) : TenderEstimateRule {
    override fun check(item: TenderEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderEstimatePalletsRangeRule(var min: Int, var max: Int) : TenderEstimateRule {
    override fun check(item: TenderEstimate): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TenderEstimateLabelLengthRule(var maxLength: Int) : TenderEstimateRule {
    override fun check(item: TenderEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderEstimateRequiredSegmentRule(var message: String) : TenderEstimateRule {
    override fun check(item: TenderEstimate): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderEstimateAllowedLabelRule(var allowed: MutableList<String>) : TenderEstimateRule {
    override fun check(item: TenderEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender estimate. */
data class TenderEstimateValidator(var rules: MutableList<TenderEstimateRule>) {
    fun validate(item: TenderEstimate): MutableList<String> {
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

    fun requireValid(item: TenderEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderEstimateValidator(): TenderEstimateValidator {
    return TenderEstimateValidator(
        mutableListOf<TenderEstimateRule>(
            TenderEstimateCodeLengthRule(16),
            TenderEstimatePalletsRangeRule(120, 86400),
            TenderEstimateLabelLengthRule(255),
            TenderEstimateRequiredSegmentRule("tender estimate needs this field"),
            TenderEstimateAllowedLabelRule(mutableListOf<String>("adaptive", "strict", "fallback")),
        ),
    )
}
