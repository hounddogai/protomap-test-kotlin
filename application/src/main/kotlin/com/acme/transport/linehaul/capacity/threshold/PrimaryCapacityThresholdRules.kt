package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.text.normalize

/** A rule a primary capacity threshold must pass. Check returns its problems, or none. */
interface PrimaryCapacityThresholdRule {
    fun check(item: PrimaryCapacityThreshold): MutableList<String>
}

data class PrimaryCapacityThresholdRequiredNameRule(var message: String) : PrimaryCapacityThresholdRule {
    override fun check(item: PrimaryCapacityThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityThresholdAllowedNameRule(
    var allowed: MutableList<String>,
) : PrimaryCapacityThresholdRule {
    override fun check(item: PrimaryCapacityThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityThresholdCodePrefixRule(var prefix: String) : PrimaryCapacityThresholdRule {
    override fun check(item: PrimaryCapacityThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityThresholdCodeLengthRule(var maxLength: Int) : PrimaryCapacityThresholdRule {
    override fun check(item: PrimaryCapacityThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary capacity threshold. */
data class PrimaryCapacityThresholdValidator(var rules: MutableList<PrimaryCapacityThresholdRule>) {
    fun validate(item: PrimaryCapacityThreshold): MutableList<String> {
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

    fun requireValid(item: PrimaryCapacityThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCapacityThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCapacityThresholdValidator(): PrimaryCapacityThresholdValidator {
    return PrimaryCapacityThresholdValidator(
        mutableListOf<PrimaryCapacityThresholdRule>(
            PrimaryCapacityThresholdRequiredNameRule("primary capacity threshold needs an entry"),
            PrimaryCapacityThresholdAllowedNameRule(mutableListOf<String>("fallback", "global")),
            PrimaryCapacityThresholdCodePrefixRule("OPS"),
            PrimaryCapacityThresholdCodeLengthRule(120),
        ),
    )
}
