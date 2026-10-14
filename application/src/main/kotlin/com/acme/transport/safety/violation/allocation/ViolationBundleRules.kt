package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.text.normalize

/** A rule a violation bundle must pass. Check returns its problems, or none. */
interface ViolationBundleRule {
    fun check(item: ViolationBundle): MutableList<String>
}

data class ViolationBundleAllowedNameRule(var allowed: MutableList<String>) : ViolationBundleRule {
    override fun check(item: ViolationBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ViolationBundleBudgetRangeRule(var min: Double, var max: Double) : ViolationBundleRule {
    override fun check(item: ViolationBundle): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class ViolationBundleChecksumLengthRule(var maxLength: Int) : ViolationBundleRule {
    override fun check(item: ViolationBundle): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationBundleRequiredCodeRule(var message: String) : ViolationBundleRule {
    override fun check(item: ViolationBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationBundleRequiredNameRule(var message: String) : ViolationBundleRule {
    override fun check(item: ViolationBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationBundleRequiredChecksumRule(var message: String) : ViolationBundleRule {
    override fun check(item: ViolationBundle): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation bundle. */
data class ViolationBundleValidator(var rules: MutableList<ViolationBundleRule>) {
    fun validate(item: ViolationBundle): MutableList<String> {
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

    fun requireValid(item: ViolationBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationBundleValidator(): ViolationBundleValidator {
    return ViolationBundleValidator(
        mutableListOf<ViolationBundleRule>(
            ViolationBundleAllowedNameRule(mutableListOf<String>("seasonal", "default")),
            ViolationBundleBudgetRangeRule(0.2, 3622.1),
            ViolationBundleChecksumLengthRule(86400),
            ViolationBundleRequiredCodeRule("violation bundle needs this field"),
            ViolationBundleRequiredNameRule("violation bundle needs this field"),
            ViolationBundleRequiredChecksumRule("violation bundle needs a value"),
        ),
    )
}
