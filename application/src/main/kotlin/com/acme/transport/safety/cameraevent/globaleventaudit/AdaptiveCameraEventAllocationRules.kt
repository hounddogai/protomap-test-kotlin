package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.kit.text.normalize

/** A rule a adaptive camera event allocation must pass. Check returns its problems, or none. */
interface AdaptiveCameraEventAllocationRule {
    fun check(item: AdaptiveCameraEventAllocation): MutableList<String>
}

data class AdaptiveCameraEventAllocationAllowedLabelRule(
    var allowed: MutableList<String>,
) : AdaptiveCameraEventAllocationRule {
    override fun check(item: AdaptiveCameraEventAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCameraEventAllocationCodeLengthRule(var maxLength: Int) : AdaptiveCameraEventAllocationRule {
    override fun check(item: AdaptiveCameraEventAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCameraEventAllocationLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveCameraEventAllocationRule {
    override fun check(item: AdaptiveCameraEventAllocation): MutableList<String> {
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

data class AdaptiveCameraEventAllocationRetriesRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveCameraEventAllocationRule {
    override fun check(item: AdaptiveCameraEventAllocation): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCameraEventAllocationLabelLengthRule(
    var maxLength: Int,
) : AdaptiveCameraEventAllocationRule {
    override fun check(item: AdaptiveCameraEventAllocation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive camera event allocation. */
data class AdaptiveCameraEventAllocationValidator(var rules: MutableList<AdaptiveCameraEventAllocationRule>) {
    fun validate(item: AdaptiveCameraEventAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveCameraEventAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCameraEventAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCameraEventAllocationValidator(): AdaptiveCameraEventAllocationValidator {
    return AdaptiveCameraEventAllocationValidator(
        mutableListOf<AdaptiveCameraEventAllocationRule>(
            AdaptiveCameraEventAllocationAllowedLabelRule(mutableListOf<String>("lenient", "batched", "capped")),
            AdaptiveCameraEventAllocationCodeLengthRule(86400),
            AdaptiveCameraEventAllocationLoadKgRangeRule(2.2, 4740.9),
            AdaptiveCameraEventAllocationRetriesRangeRule(64, 1000),
            AdaptiveCameraEventAllocationLabelLengthRule(120),
        ),
    )
}
