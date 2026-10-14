package com.acme.transport.linehaul.tender.preference

import com.acme.transport.kit.text.normalize

/** A rule a manual tender estimate must pass. Check returns its problems, or none. */
interface ManualTenderEstimateRule {
    fun check(item: ManualTenderEstimate): MutableList<String>
}

data class ManualTenderEstimateScoreRangeRule(var min: Double, var max: Double) : ManualTenderEstimateRule {
    override fun check(item: ManualTenderEstimate): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderEstimateRequiredLocaleRule(var message: String) : ManualTenderEstimateRule {
    override fun check(item: ManualTenderEstimate): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderEstimateAllowedCodeRule(var allowed: MutableList<String>) : ManualTenderEstimateRule {
    override fun check(item: ManualTenderEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderEstimateNameLengthRule(var maxLength: Int) : ManualTenderEstimateRule {
    override fun check(item: ManualTenderEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderEstimateRequiredNameRule(var message: String) : ManualTenderEstimateRule {
    override fun check(item: ManualTenderEstimate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderEstimateCodePrefixRule(var prefix: String) : ManualTenderEstimateRule {
    override fun check(item: ManualTenderEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual tender estimate. */
data class ManualTenderEstimateValidator(var rules: MutableList<ManualTenderEstimateRule>) {
    fun validate(item: ManualTenderEstimate): MutableList<String> {
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

    fun requireValid(item: ManualTenderEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTenderEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTenderEstimateValidator(): ManualTenderEstimateValidator {
    return ManualTenderEstimateValidator(
        mutableListOf<ManualTenderEstimateRule>(
            ManualTenderEstimateScoreRangeRule(4.5, 4260.8),
            ManualTenderEstimateRequiredLocaleRule("manual tender estimate needs this field"),
            ManualTenderEstimateAllowedCodeRule(mutableListOf<String>("regional", "default", "pending")),
            ManualTenderEstimateNameLengthRule(64),
            ManualTenderEstimateRequiredNameRule("manual tender estimate needs a value"),
            ManualTenderEstimateCodePrefixRule("ACM"),
        ),
    )
}
