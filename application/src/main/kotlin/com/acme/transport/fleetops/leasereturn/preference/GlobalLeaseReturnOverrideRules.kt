package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.text.normalize

/** A rule a global lease return override must pass. Check returns its problems, or none. */
interface GlobalLeaseReturnOverrideRule {
    fun check(item: GlobalLeaseReturnOverride): MutableList<String>
}

data class GlobalLeaseReturnOverrideGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : GlobalLeaseReturnOverrideRule {
    override fun check(item: GlobalLeaseReturnOverride): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class GlobalLeaseReturnOverrideRequiredPhoneRule(var message: String) : GlobalLeaseReturnOverrideRule {
    override fun check(item: GlobalLeaseReturnOverride): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalLeaseReturnOverrideAllowedSummaryRule(
    var allowed: MutableList<String>,
) : GlobalLeaseReturnOverrideRule {
    override fun check(item: GlobalLeaseReturnOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLeaseReturnOverridePhoneLengthRule(var maxLength: Int) : GlobalLeaseReturnOverrideRule {
    override fun check(item: GlobalLeaseReturnOverride): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLeaseReturnOverrideCodeLengthRule(var maxLength: Int) : GlobalLeaseReturnOverrideRule {
    override fun check(item: GlobalLeaseReturnOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLeaseReturnOverrideRequiredCodeRule(var message: String) : GlobalLeaseReturnOverrideRule {
    override fun check(item: GlobalLeaseReturnOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global lease return override. */
data class GlobalLeaseReturnOverrideValidator(var rules: MutableList<GlobalLeaseReturnOverrideRule>) {
    fun validate(item: GlobalLeaseReturnOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalLeaseReturnOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalLeaseReturnOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalLeaseReturnOverrideValidator(): GlobalLeaseReturnOverrideValidator {
    return GlobalLeaseReturnOverrideValidator(
        mutableListOf<GlobalLeaseReturnOverrideRule>(
            GlobalLeaseReturnOverrideGraceDaysRangeRule(32, 1000),
            GlobalLeaseReturnOverrideRequiredPhoneRule("global lease return override needs an entry"),
            GlobalLeaseReturnOverrideAllowedSummaryRule(mutableListOf<String>("legacy", "primary", "rolling")),
            GlobalLeaseReturnOverridePhoneLengthRule(80),
            GlobalLeaseReturnOverrideCodeLengthRule(1000),
            GlobalLeaseReturnOverrideRequiredCodeRule("global lease return override needs an entry"),
        ),
    )
}
