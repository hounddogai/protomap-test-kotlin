package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a dispute threshold must pass. Check returns its problems, or none. */
interface DisputeThresholdRule {
    fun check(item: DisputeThreshold): MutableList<String>
}

data class DisputeThresholdCreatedAtRangeRule(var min: Int, var max: Int) : DisputeThresholdRule {
    override fun check(item: DisputeThreshold): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class DisputeThresholdCodePrefixRule(var prefix: String) : DisputeThresholdRule {
    override fun check(item: DisputeThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DisputeThresholdRequiredLabelRule(var message: String) : DisputeThresholdRule {
    override fun check(item: DisputeThreshold): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeThresholdCodeLengthRule(var maxLength: Int) : DisputeThresholdRule {
    override fun check(item: DisputeThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeThresholdWeightRangeRule(var min: Double, var max: Double) : DisputeThresholdRule {
    override fun check(item: DisputeThreshold): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeThresholdAllowedDepotRule(var allowed: MutableList<String>) : DisputeThresholdRule {
    override fun check(item: DisputeThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute threshold. */
data class DisputeThresholdValidator(var rules: MutableList<DisputeThresholdRule>) {
    fun validate(item: DisputeThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeThresholdValidator(): DisputeThresholdValidator {
    return DisputeThresholdValidator(
        mutableListOf<DisputeThresholdRule>(
            DisputeThresholdCreatedAtRangeRule(120, 255),
            DisputeThresholdCodePrefixRule("REF"),
            DisputeThresholdRequiredLabelRule("dispute threshold needs a value"),
            DisputeThresholdCodeLengthRule(255),
            DisputeThresholdWeightRangeRule(8.8, 421.3),
            DisputeThresholdAllowedDepotRule(mutableListOf<String>("weighted", "batched")),
        ),
    )
}
