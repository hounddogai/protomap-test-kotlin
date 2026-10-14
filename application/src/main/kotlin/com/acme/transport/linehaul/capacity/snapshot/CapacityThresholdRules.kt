package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a capacity threshold must pass. Check returns its problems, or none. */
interface CapacityThresholdRule {
    fun check(item: CapacityThreshold): MutableList<String>
}

data class CapacityThresholdCodePrefixRule(var prefix: String) : CapacityThresholdRule {
    override fun check(item: CapacityThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CapacityThresholdRequiredCodeRule(var message: String) : CapacityThresholdRule {
    override fun check(item: CapacityThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityThresholdAllowedRegionRule(var allowed: MutableList<String>) : CapacityThresholdRule {
    override fun check(item: CapacityThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class CapacityThresholdNameLengthRule(var maxLength: Int) : CapacityThresholdRule {
    override fun check(item: CapacityThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity threshold. */
data class CapacityThresholdValidator(var rules: MutableList<CapacityThresholdRule>) {
    fun validate(item: CapacityThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityThresholdValidator(): CapacityThresholdValidator {
    return CapacityThresholdValidator(
        mutableListOf<CapacityThresholdRule>(
            CapacityThresholdCodePrefixRule("REF"),
            CapacityThresholdRequiredCodeRule("capacity threshold needs this field"),
            CapacityThresholdAllowedRegionRule(mutableListOf<String>("fallback", "tiered", "fallback")),
            CapacityThresholdNameLengthRule(120),
        ),
    )
}
