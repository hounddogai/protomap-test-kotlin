package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.text.normalize

/** A rule a violation profile must pass. Check returns its problems, or none. */
interface ViolationProfileRule {
    fun check(item: ViolationProfile): MutableList<String>
}

data class ViolationProfileCodePrefixRule(var prefix: String) : ViolationProfileRule {
    override fun check(item: ViolationProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ViolationProfileAllowedCodeRule(var allowed: MutableList<String>) : ViolationProfileRule {
    override fun check(item: ViolationProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ViolationProfileLabelLengthRule(var maxLength: Int) : ViolationProfileRule {
    override fun check(item: ViolationProfile): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationProfileCostRangeRule(var min: Double, var max: Double) : ViolationProfileRule {
    override fun check(item: ViolationProfile): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class ViolationProfileRequiredCodeRule(var message: String) : ViolationProfileRule {
    override fun check(item: ViolationProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation profile. */
data class ViolationProfileValidator(var rules: MutableList<ViolationProfileRule>) {
    fun validate(item: ViolationProfile): MutableList<String> {
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

    fun requireValid(item: ViolationProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationProfileValidator(): ViolationProfileValidator {
    return ViolationProfileValidator(
        mutableListOf<ViolationProfileRule>(
            ViolationProfileCodePrefixRule("ACM"),
            ViolationProfileAllowedCodeRule(mutableListOf<String>("default", "tiered", "global")),
            ViolationProfileLabelLengthRule(3600),
            ViolationProfileCostRangeRule(1.0, 2040.0),
            ViolationProfileRequiredCodeRule("violation profile needs an entry"),
        ),
    )
}
