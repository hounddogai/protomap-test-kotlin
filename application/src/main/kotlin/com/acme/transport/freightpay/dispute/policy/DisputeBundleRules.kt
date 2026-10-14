package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.kit.text.normalize

/** A rule a dispute bundle must pass. Check returns its problems, or none. */
interface DisputeBundleRule {
    fun check(item: DisputeBundle): MutableList<String>
}

data class DisputeBundleAllowedChecksumRule(var allowed: MutableList<String>) : DisputeBundleRule {
    override fun check(item: DisputeBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class DisputeBundleCodeLengthRule(var maxLength: Int) : DisputeBundleRule {
    override fun check(item: DisputeBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeBundleCodePrefixRule(var prefix: String) : DisputeBundleRule {
    override fun check(item: DisputeBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DisputeBundleContactEmailLengthRule(var maxLength: Int) : DisputeBundleRule {
    override fun check(item: DisputeBundle): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeBundleChecksumLengthRule(var maxLength: Int) : DisputeBundleRule {
    override fun check(item: DisputeBundle): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeBundleGraceDaysRangeRule(var min: Int, var max: Int) : DisputeBundleRule {
    override fun check(item: DisputeBundle): MutableList<String> {
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

/** Runs every rule of a dispute bundle. */
data class DisputeBundleValidator(var rules: MutableList<DisputeBundleRule>) {
    fun validate(item: DisputeBundle): MutableList<String> {
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

    fun requireValid(item: DisputeBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeBundleValidator(): DisputeBundleValidator {
    return DisputeBundleValidator(
        mutableListOf<DisputeBundleRule>(
            DisputeBundleAllowedChecksumRule(mutableListOf<String>("default")),
            DisputeBundleCodeLengthRule(86400),
            DisputeBundleCodePrefixRule("REF"),
            DisputeBundleContactEmailLengthRule(255),
            DisputeBundleChecksumLengthRule(3600),
            DisputeBundleGraceDaysRangeRule(120, 3600),
        ),
    )
}
