package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.kit.text.normalize

/** A rule a drop estimate must pass. Check returns its problems, or none. */
interface DropEstimateRule {
    fun check(item: DropEstimate): MutableList<String>
}

data class DropEstimateLabelLengthRule(var maxLength: Int) : DropEstimateRule {
    override fun check(item: DropEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropEstimateCodePrefixRule(var prefix: String) : DropEstimateRule {
    override fun check(item: DropEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DropEstimateCreatedAtRangeRule(var min: Int, var max: Int) : DropEstimateRule {
    override fun check(item: DropEstimate): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DropEstimateRequiredCodeRule(var message: String) : DropEstimateRule {
    override fun check(item: DropEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropEstimateCodeLengthRule(var maxLength: Int) : DropEstimateRule {
    override fun check(item: DropEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropEstimateAllowedLabelRule(var allowed: MutableList<String>) : DropEstimateRule {
    override fun check(item: DropEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop estimate. */
data class DropEstimateValidator(var rules: MutableList<DropEstimateRule>) {
    fun validate(item: DropEstimate): MutableList<String> {
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

    fun requireValid(item: DropEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultDropEstimateValidator(): DropEstimateValidator {
    return DropEstimateValidator(
        mutableListOf<DropEstimateRule>(
            DropEstimateLabelLengthRule(86400),
            DropEstimateCodePrefixRule("REF"),
            DropEstimateCreatedAtRangeRule(255, 3600),
            DropEstimateRequiredCodeRule("drop estimate needs an entry"),
            DropEstimateCodeLengthRule(80),
            DropEstimateAllowedLabelRule(mutableListOf<String>("weighted", "default")),
        ),
    )
}
